import static com.lombardrisk.pipeline.Credentials.AWS

def put(Map args) {
    String localPath = args.localPath
    String remotePath = args.remotePath
    String bucket = args.bucket
    boolean serverSideEncryption = args.encrypt ? args.encrypt : false
    boolean recursive = args.recursive ? args.recursive : false

    String cmd = "s3 cp $localPath s3://$bucket/$remotePath --no-progress"

    if (serverSideEncryption) {
        cmd += ' --sse AES256'
    }

    if (recursive) {
        cmd += ' --recursive'
    }

    echo "Copying files to bucket [$bucket]"

    execute(cmd)
}

def get(Map args) {
    String localPath = args.localPath
    String remotePath = args.remotePath
    String bucket = args.bucket
    boolean recursive = args.recursive ? args.recursive : false

    String cmd = "s3 cp s3://$bucket/$remotePath $localPath  --no-progress"

    if (recursive) {
        cmd += ' --recursive'
    }

    echo "Retrieving files from bucket [$bucket]"

    execute(cmd)
}

def delete(Map args) {
    String remotePath = args.remotePath
    String bucket = args.bucket
    boolean recursive = args.recursive ? args.recursive : false

    String cmd = "s3 rm s3://$bucket/$remotePath"

    if (recursive) {
        cmd += ' --recursive'
    }

    echo "Deleting files from bucket [$bucket]"

    execute(cmd)
}

private def execute(String cmd) {
    withCredentials([usernamePassword(
            credentialsId: AWS,
            usernameVariable: 'AWS_ACCESS_KEY_ID',
            passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {

        String localBin = "${env.HOME}/.local/bin"

        withEnv(["PATH+LOCAL_BIN=$localBin"]) {
            sh "aws $cmd"
        }
    }
}