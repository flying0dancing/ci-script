void call(Map passedArgs) {
    def args = [cluster: false, skipReinstall: false, skipS3DeployDownload: false, upgradePlatformTools: false]
    args << passedArgs

    def buildVersion = findBuildVersion(args.candidateReleasePath)
    def rootDir = "~/fcr-engine/platform-tools"
    def installPath = "${rootDir}/${buildVersion}"

    if (args.skipReinstall && common.isExisting(installPath)) {
        echo "Skipping reinstall of platform-tools ${buildVersion}"

        start(installPath, buildVersion)
        return
    }
    echo "Install platform-tools ${buildVersion}"

    downloadAndUnzipInstaller(buildVersion, args.candidateReleasePath, args.skipS3DeployDownload)

    def systemProperties = "${pwd()}/system.properties"

    if (args.upgradePlatformTools) {
        install(installPath, buildVersion, args.cluster, systemProperties)

        upgrade(installPath, buildVersion, rootDir)
    } else {
        stop()

        install(installPath, buildVersion, args.cluster, systemProperties)

        start(installPath, buildVersion)
    }

    cleanUp(args, rootDir, systemProperties)
}

private String findBuildVersion(String candidateReleasePath) {
    def match = candidateReleasePath =~ 'platform-tools/(.*)/'
    return match[0][1]
}

private void downloadAndUnzipInstaller(String buildVersion, String candidateReleasePath, boolean skipS3DeployDownload) {
    if (!skipS3DeployDownload) {
        s3Deploy.download candidateReleasePath
    }

    quietSh 'rm -rf ./fcr-engine-platform-tools-*'

    unzip zipFile: candidateReleasePath, dir: "fcr-engine-platform-tools-${buildVersion}"
}

private void stop() {
    def platformToolsService = quietSh 'systemctl list-units | grep fcr-engine-platform-tools || true'

    if (!platformToolsService.isEmpty()) {
        def currentVersionDescription = quietSh 'systemctl show fcr-engine-platform-tools -p Description | sed -e "s/^Description=//"'

        echo "Stop ${currentVersionDescription}"
        quietSh 'sudo systemctl stop fcr-engine-platform-tools'
    } else {
        echo 'fcr-engine-platform-tools service does not exist'
    }
}

private void install(String installPath, String buildVersion, boolean clusterMode, String systemProperties) {
    dir("fcr-engine-platform-tools-${buildVersion}") {
        def installScript = clusterMode ? 'cluster-install.sh' : 'install.sh'

        sh """chmod u+x *.sh
              ./${installScript} -p ${systemProperties}"""
        sh "sudo ${installPath}/bin/service.sh install || true"
    }
}

private void upgrade(String installPath, String buildVersion, String rootDir) {
    def currentVersion = quietSh(script: "ls -t ${rootDir} | grep -v '$buildVersion'", returnStdout: true).split(/\s+/)[0];

    echo "Upgrading platform-tools from $currentVersion to $buildVersion"

    sh(script: "$installPath/bin/upgrade.sh upgrade $rootDir/$currentVersion")

    retry(10) {
        serviceScript.runAndVerifyUp "${installPath}/bin/status.sh"
    }
}

private void start(String installPath, String buildVersion) {
    echo "Start platform-tools ${buildVersion}"

    quietSh "sudo ${installPath}/bin/service.sh start"

    retry(10) {
        serviceScript.runAndVerifyUp "${installPath}/bin/status.sh"
    }
    sh "${installPath}/bin/service.sh status"
}

private void cleanUp(Map args, String rootDir, String systemProperties) {
    def versionsToRemove = common.removeOldReleases(rootDir)

    if (args.cluster) {
        def props = readProperties file: systemProperties
        def hosts = props.hosts.split(',')
        echo "Clean hosts ${hosts}"

        hosts.each { host ->
            sh """ssh ${host} bash -c "'
                    cd ${rootDir} && rm -rf ${versionsToRemove} 
                    find -type d -name 'fcr-engine-platform-tools-*' | xargs rm -rf
                  '"
               """
        }
    } else {
        quietSh """rm -rf ${args.candidateReleasePath}
                   rm -rf ./fcr-engine-platform-tools-*"""
    }
}