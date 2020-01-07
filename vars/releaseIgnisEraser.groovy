void call(Map passedArgs) {
    def args = [skipS3DeployDownload: false]
    args << passedArgs

    def ignisEraserDir = "${args.homeDir}/ignis-eraser"
    echo "Install Ignis Eraser in [${ignisEraserDir}]"

    ifFileExists("${ignisEraserDir}/bin/ignis-eraser.sh") {
        stopIgnisEraser(ignisEraserDir)
        sh "rm -rf ${ignisEraserDir}"
    }

    downloadAndUnzipInstaller(args.candidateReleasePath, args.skipS3DeployDownload)

    sh """mkdir -p ${ignisEraserDir}
               cp -r ./fcr-engine-ignis-eraser*/* ${ignisEraserDir}
               cp eraser.properties ${ignisEraserDir}/conf/eraser.properties            
               chmod u+x -R ${ignisEraserDir}/bin"""

    startIgnisEraser(ignisEraserDir)

    quietSh """rm -rf ${args.candidateReleasePath}
               rm -rf ./fcr-engine-ignis-eraser-*"""
}

private void startIgnisEraser(String ignisEraserDir) {
    echo "Starting Ignis Eraser"
    sh "${ignisEraserDir}/bin/ignis-eraser.sh start"
}

private void stopIgnisEraser(String ignisEraserDir) {
    echo "Stopping Ignis Eraser"
    sh "${ignisEraserDir}/bin/ignis-eraser.sh stop"
}

private void downloadAndUnzipInstaller(String candidateReleasePath, boolean skipS3DeployDownload) {
    if (!skipS3DeployDownload) {
        s3Deploy.download candidateReleasePath
    }

    quietSh 'rm -rf ./fcr-engine-ignis-eraser-*'

    unzip zipFile: candidateReleasePath
}
