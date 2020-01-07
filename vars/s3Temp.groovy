
void upload(String candidateRelease) {
//    aws.s3Temp().fcrEngine.upload(candidateRelease, candidateRelease)
    s3Cli.put(localPath: candidateRelease, remotePath: toFullPath(candidateRelease), bucket: aws.S3.Temp.bucketName)
}

void upload(String localCandidateReleasePath, String s3CandidateReleasePath) {
//    aws.s3Temp().fcrEngine.upload(localCandidateReleasePath, s3CandidateReleasePath)
    s3Cli.put(localPath: localCandidateReleasePath, remotePath: toFullPath(s3CandidateReleasePath), bucket: aws.S3.Temp.bucketName)
}

void download(String candidateRelease) {
//    aws.s3Temp().fcrEngine.download(candidateRelease, candidateRelease)
    s3Cli.get(localPath: candidateRelease, remotePath: toFullPath(candidateRelease), bucket: aws.S3.Temp.bucketName)
}

void delete(String candidateRelease) {
    s3Cli.delete(remotePath: toFullPath(candidateRelease), bucket: aws.S3.Temp.bucketName)
}

private static String toFullPath(remotePath) {
    return "FCR-Engine/Releases/CandidateReleases/$remotePath"
}