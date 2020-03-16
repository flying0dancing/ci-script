#!groovy
@Library('pipeline-libs') _
import static com.lombardrisk.pipeline.Credentials.AWS

FILE_OPERATORS= """
upload2remote
download2local
remove4remote"""

pipeline {
    agent { label 'PRODUCT-CI-SHA-LOCAL1' }
    options {
        timeout(time: 60, unit: 'MINUTES')
    }
    parameters {
        string name: 'S3BUCKET',
                description: 'remote repository. Required',
                defaultValue: 'lrm-deploy'

        string name: 'REMOTEPATH',
                description: 'remote repository path. Required',
                defaultValue: 'arproduct/hkma/CandidateReleases/5.32.0/b85/'

        string name: 'LOCALPATH',
                description: 'local repository path',
                defaultValue: '/home/test/repository/ARProduct/hkma/candidate-release/5.32.0/b85/'

        booleanParam name: 'RECURSIVE',
                description: ' recursive operate upload or download or delete all installers under some path',
                defaultValue: true

        string name: 'INSTALLERNAMES',
                description: 'names of installers. Many installers can join with colon. Like CE_DPB_v5.32.0-b85.zip:CE_DPB_v5.32.0-b85_sign.lrm.',
                defaultValue: '*'

        choice name: 'FILEOPERATOR',
                choices: FILE_OPERATORS,
                description: 'upload or download or delete operators. Required'
    }
    stages {
        stage('checkout'){
            steps{
                echo "start job B ${JOB_URL}"
                echo "branch number: ${env.BUILD_NUMBER}"
                echo "bucket: ${S3BUCKET}"
                echo "remote: ${REMOTEPATH}"
                echo "local: ${LOCALPATH}"
                echo "installers: ${INSTALLERNAMES}"
                echo "operator: ${FILEOPERATOR}"
                echo "recursive $RECURSIVE"
                sh 'pwd'
            }
        }
        stage('operate packages'){
            steps{
                script{
                    echo "operate packages"
                    selectOperator(params.FILEOPERATOR, params.S3BUCKET, params.REMOTEPATH, params.LOCALPATH, params.RECURSIVE, params.INSTALLERNAMES)
                }

            }
        }

    }
    post {
        always {
            echo 'This will always run'
        }
        success {
            echo 'This will run only if successful'

        }
        failure {
            echo 'This will run only if failed'
        }
        unstable {
            echo 'This will run only if the run was marked as unstable'
        }
        changed {
            echo 'This will run only if the state of the Pipeline has changed'
            echo 'For example, if the Pipeline was previously failing but is now successful'
        }
    }
}

def selectOperator(String fileOperator,String bucket, String remotePath, String localPath, boolean recursive=false, String packageNames=null){
    if(!localPath.startsWith('/home/test/repository')){
        localPath='/home/test/repository/'+localPath
    }
    switch(fileOperator){
        case 'upload2remote':
            upload2Remote( bucket, remotePath, localPath, recursive, packageNames)
            break
        case 'download2local':
            download2Local(bucket, remotePath, localPath, recursive, packageNames)
            break
        case 'remove4remote':
            delete(bucket, remotePath, recursive, packageNames)
            break
        default:
            download2Local(bucket,remotePath, localPath, recursive, packageNames)
    }
}

def upload2Remote(String bucket, String remotePath, String localPath, boolean recursive, String packageNames) {
    if(!isEmptyOrNullOrStar(packageNames)){
        String[] packageNameArr=packageNames.split(':')
        for(String packageName in packageNameArr){
            println "Copying $packageName to bucket [$bucket]"
            execute("s3 cp $localPath$packageName s3://$bucket/$remotePath$packageName  --no-progress ") //ssl error  --no-verify-ssl
        }
    }else{
        String cmd = "s3 cp $localPath s3://$bucket/$remotePath --no-progress"
        if (recursive) {
            cmd += ' --recursive'
        }
        echo "Copying files to bucket [$bucket]"
        execute(cmd)
    }

}

def download2Local(String bucket, String remotePath, String localPath, boolean recursive, String packageNames) {
    if(!isEmptyOrNullOrStar(packageNames)){
        String[] packageNameArr=packageNames.split(':')
        for(String packageName in packageNameArr){
            println "Retrieving $packageName from bucket [$bucket]"
            execute("s3 cp s3://$bucket/$remotePath$packageName $localPath$packageName --no-progress ") //ssl error  --no-verify-ssl
        }
    }else{
        String cmd = "s3 cp s3://$bucket/$remotePath $localPath  --no-progress"
        if (recursive) {
            cmd += ' --recursive'
        }
        echo "Retrieving files from bucket [$bucket]"
        execute(cmd)
    }

}

def delete(String bucket, String remotePath, boolean recursive, String packageNames) {
    if(!isEmptyOrNullOrStar(packageNames)){
        String[] packageNameArr=packageNames.split(':')
        for(String packageName in packageNameArr){
            println "Deleting $packageName from bucket [$bucket]"
            execute("s3 rm s3://$bucket/$remotePath$packageName ")
        }
    }else{
        String cmd = "s3 rm s3://$bucket/$remotePath"
        if (recursive) {
            cmd += ' --recursive'
        }
        echo "Deleting files from bucket [$bucket]"
        execute(cmd)
    }

}

private def isEmptyOrNullOrStar(String str){
    if(str==null || str.trim().equals('') || str.trim().equals('*')){
        return true
    }else{
        return false
    }
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