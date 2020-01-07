void call(Map passedArgs) {
    def args = [skipS3DeployDownload: false]
    args << passedArgs

    if (!args.skipS3DeployDownload) {
        aws.s3Deploy().agileReporter.download(args.installerJar, args.installerJar)
    }

    String agileReporterDir = "${args.homeDir}/AgileREPORTER"
    echo "Install ${args.installerJar}"

    sh """java -jar ${args.installerJar} -options ocelot.properties
          nohup ${agileReporterDir}/bin/start.sh &"""

    retry(12) {
        sleep 10
        ar.checkAgileReporterUp("http://${args.arHost}:${args.arPort}")
    }
    sh "rm -rf ${args.installerJar}"
}
