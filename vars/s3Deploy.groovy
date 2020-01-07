void upload(String candidateRelease) {
//    aws.s3Deploy().fcrEngine.upload(candidateRelease, candidateRelease)
    s3Cli.put(localPath: candidateRelease, remotePath: toFullPath(candidateRelease), bucket: aws.S3.Deploy.bucketName)
}

void upload(String localItem, String location) {
//    aws.s3Deploy().fcrEngine.upload(localItem, location)
    s3Cli.put(localPath: localItem, remotePath: toFullPath(location), bucket: aws.S3.Deploy.bucketName)
}

void download(String candidateRelease) {
//    aws.s3Deploy().fcrEngine.download(candidateRelease, candidateRelease)
    s3Cli.get(localPath: candidateRelease, remotePath: toFullPath(candidateRelease), bucket: aws.S3.Deploy.bucketName)
}

void createLink(String candidateRelease) {
    aws.s3Deploy().fcrEngine.createLink(candidateRelease, candidateRelease)
}

def findAgileReporterReleases(String glob, String version = '') {
    def fileGlob = glob
    if (!version.isEmpty()) {
        fileGlob = glob.repl
    }
    return aws.s3Deploy().agileReporter.findFiles(fileGlob)
}

private static String toFullPath(remotePath) {
    return "FCR-Engine/Releases/CandidateReleases/$remotePath"
}