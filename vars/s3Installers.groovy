import static com.lombardrisk.pipeline.Credentials.AWS

import com.lombardrisk.pipeline.aws.s3.S3Upload

void upload(Map args) {
//    new S3Upload(steps: this, bucketName: bucketName(), region: 'eu-west-1', 'releasePath': 'fcr-engine')
//            .upload(args.localFile, args.remoteFile)

    s3Cli.put(localPath: args.localFile, remotePath: toFullPath(args.remoteFile), bucket: bucketName())
}

void cleanup(String buildVersion) {
//    withAWS(credentials: AWS, region: 'eu-west-1') {
//        s3Delete([
//                bucket: bucketName(),
//                path  : "fcr-engine/$buildVersion/",
//                acl   : 'BucketOwnerFullControl'
//        ])
//    }

    s3Cli.delete(remotePath: toFullPath(buildVersion + "/"), bucket: bucketName(), recursive: true)
}

static String bucketName() {
    return 'lrm-installers-947109822564';
}

private static String toFullPath(remotePath) {
    return "fcr-engine/$remotePath"
}