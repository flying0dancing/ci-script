void call(Map passedArgs) {
    def args = [skipS3DeployDownload: false]
    args << passedArgs

    def designStudioDir = "${args.homeDir}/design-studio"
    echo "Install Design Studio in [${designStudioDir}]"

    ifFileExists("${designStudioDir}/bin/design-studio.sh") {
        stopDesignStudio(designStudioDir)
        sh "rm -rf ${designStudioDir}"
    }
    downloadAndUnzipInstaller(args.candidateReleasePath, args.skipS3DeployDownload)

    sh """mkdir -p ${designStudioDir}
               cp -r ./fcr-engine-design-studio*/* ${designStudioDir}
               cp flyway.conf ${designStudioDir}/lib/flyway-*/conf/flyway.conf
               cp application.properties ${designStudioDir}/conf/application.properties            
               chmod u+x -R ${designStudioDir}
               ${designStudioDir}/bin/migrate.sh"""
    ifFileExists('feature.file.properties') {
        sh "cp feature.file.properties ${designStudioDir}/conf/feature.file.properties"
    }
    startDesignStudio(designStudioDir)

    retry(10) {
        serviceScript.runAndVerifyUp "${designStudioDir}/bin/design-studio.sh status"

        checkHealthy()
    }
    quietSh """rm -rf ${args.candidateReleasePath}
               rm -rf ./fcr-engine-design-studio-*"""
}

private void stopDesignStudio(String designStudioDir) {
    def designStudioService = quietSh 'systemctl list-units | grep design-studio || true'

    if (!designStudioService.isEmpty()) {
        echo "Stopping Design Studio service"
        sh """sudo ${designStudioDir}/bin/service.sh stop
                sleep 5"""
        sh "sudo ${designStudioDir}/bin/service.sh uninstall"
    } else {
        echo "Stopping Design Studio process"
        sh """${designStudioDir}/bin/design-studio.sh stop
              sleep 5"""
    }
}

private void startDesignStudio(String designStudioDir) {
    echo "Starting Design Studio service"
    sh "sudo ${designStudioDir}/bin/service.sh install"
    sh "sudo ${designStudioDir}/bin/service.sh start || true"
}

private void downloadAndUnzipInstaller(String candidateReleasePath, boolean skipS3DeployDownload) {
    if (!skipS3DeployDownload) {
        s3Deploy.download candidateReleasePath
    }

    quietSh 'rm -rf ./fcr-engine-design-studio-*'

    unzip zipFile: candidateReleasePath
}

private void checkHealthy() {
    sleep 5

    def statusJson = quietSh script: """
        curl --silent --fail --show-error \
             --request GET \
             --header 'Content-Type: application/json' \
             http://localhost:8082/actuator/health""", returnStdout: true

    if (!statusJson.contains('"UP"')) {
        echo 'Design Studio Web App did not start up correctly!'
        error "Status: $statusJson"
    }
}