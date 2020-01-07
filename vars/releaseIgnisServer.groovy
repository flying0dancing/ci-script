void call(Map passedArgs) {
    def args = [skipReinstall: false, skipS3DeployDownload: false]
    args << passedArgs

    def buildVersion = findBuildVersion(args.candidateReleasePath)
    def rootDir = "~/fcr-engine/ignis-server"
    def installPath = "${rootDir}/${buildVersion}"

    if (args.skipReinstall && common.isExisting(installPath)) {
        echo "Skipping reinstall of ignis-server ${buildVersion}"

        start(installPath, buildVersion)
        return
    }
    echo "Install ignis-server ${buildVersion}"

    downloadAndUnzipInstaller(buildVersion, args.candidateReleasePath, args.skipS3DeployDownload)

    stop()
    install(installPath, buildVersion)
    start(installPath, buildVersion)

    cleanUp(args.candidateReleasePath, rootDir)
    echo "Finished releasing Ignis Server"
}

private void downloadAndUnzipInstaller(String buildVersion, String candidateReleasePath, boolean skipS3DeployDownload) {
    if (!skipS3DeployDownload) {
        s3Deploy.download candidateReleasePath
    }

    quietSh 'rm -rf ./fcr-engine-ignis-server-*'

    unzip zipFile: candidateReleasePath, dir: "fcr-engine-ignis-server-${buildVersion}"
}

private String findBuildVersion(String candidateReleasePath) {
    def match = candidateReleasePath =~ 'ignis-server/(.*)/'
    return match[0][1]
}

private void stop() {
    def ignisServerService = quietSh 'systemctl list-units | grep fcr-engine-ignis-server || true'

    if (!ignisServerService.isEmpty()) {
        def currentVersionDescription = quietSh 'systemctl show fcr-engine-ignis-server -p Description | sed -e "s/^Description=//"'

        echo "Stop ${currentVersionDescription}"
        quietSh 'sudo systemctl stop fcr-engine-ignis-server'
    } else {
        echo 'fcr-engine-ignis-server service does not exist'
    }
}

private void install(String installPath, String buildVersion) {
    def systemProperties = "${pwd()}/system.properties"

    dir("fcr-engine-ignis-server-${buildVersion}") {
        sh """chmod u+x install.sh  
              ./install.sh -p ${systemProperties}"""
        sh "sudo ${installPath}/bin/service.sh install || true"
    }
    ifFileExists('feature.file.properties') {
        sh "cp feature.file.properties ${installPath}/ignis-server-${buildVersion}/conf/"
    }
    ifFileExists('log4j.properties') {
        sh "cp log4j.properties ${installPath}/ignis-server-${buildVersion}/conf/spark/"
    }

    String scriptletJar = 'ignis-spark/ignis-spark-script-demo/target/ignis-spark-script-demo.jar'
    ifFileExists(scriptletJar) {
        sh "cp ${scriptletJar} ${installPath}/ignis-server-${buildVersion}/lib/"
    }
}

private void start(String installPath, String buildVersion) {
    echo "Start ignis-server ${buildVersion}"

    quietSh "sudo ${installPath}/bin/service.sh start"
    def hostname = quietSh script: "hostname", returnStdout: true

    retry(10) {
        checkIgnisServerIsHealthy host: hostname.trim(), port: 8443
    }
    sh "${installPath}/bin/service.sh status"
}

private void cleanUp(String candidateReleasePath, String rootDir) {
    echo "releaseIgniServer: Clean up"
    common.removeOldReleases(rootDir)

    quietSh """rm -rf ${candidateReleasePath}
               rm -rf ./fcr-engine-ignis-server-*"""
}