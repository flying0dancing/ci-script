import static com.lombardrisk.pipeline.Credentials.AWS

/**
 * download agile reporter from S3 to local
 * @param props: get value from deploy folder's env.properties
 * @param downloadFileName
 * @return full name of download installer in local server
 */
String downloadOcelot(props,downloadFileName){
    def s3_ar_repo='AgileREPORTER/Releases/CandidateReleases/'
    def local_repo='/home/'+props['app.user']+'/'+props['ar.local.repo']
    return downloadFromS3(s3_ar_repo,props,downloadFileName,local_repo)
}

/**
 * download agile reporter product from S3 to local
 * @param projectName
 * @param props: get value from deploy folder's env.properties
 * @param downloadFileName
 * @return full name of download installer in local server
 */
String downloadARProduct(projectName,props,downloadFileName){
    def s3_ar_repo='arproduct/'+projectName+'/CandidateReleases/'
    def local_repo='/home/'+props['app.user']+'/'+props['product.local.repo']+projectName+'/candidate-release/'
    return downloadFromS3(s3_ar_repo,props,downloadFileName,local_repo)
}

/**
 * download installer from s3
 * @param s3repo: s3 path
 * @param props: get value from deploy folder's env.properties
 * @param downloadFileName: like CE_DPB_v1.0.0-b9_sign.lrm under <projectName>/candidate-release/<productVersionFolder>/
 * @param local_repo: local server's path
 * @return full name of download installer in local server
 */
String downloadFromS3(s3repo,props,downloadFileName,local_repo){
    def downloadFullName
    def downfiles
    //def local_linux=props['app.host']
    //def app_user=props['app.user']
    //def app_hostuser=app_user+'@'+local_linux
    def s3_bucket=props['s3.bucket']
    withAWS(credentials: 'aws') {
        downfiles=s3FindFiles(bucket:s3_bucket, path:s3repo, glob:"**/${downloadFileName}")
        //downfiles.each{print "${it.name},${it.path},${it.length},${it.lastModified}"}
    }
    if(downfiles){
        //withAWS(credentials: 'aws') {
        //    s3Download(bucket:s3_bucket, path:s3repo+downfiles[0].path,file:downfiles[0].path,force:true)
        //}
        String cmd = "s3 cp s3://$s3_bucket/$s3repo${downfiles[0].path} $local_repo${downfiles[0].path}  --no-progress "
        execute(cmd)
        //sh( returnStatus: true, script: 'scp '+downfiles[0].path+' '+app_hostuser+':'+downloadPath)
        echo "downloaded Agile Reporter Product completely."
        downloadFullName="$local_repo${downfiles[0].path}"
    }else{
        error "there is no packages existed in bucket server, name like ${downloadFileName}"
    }
return downloadFullName
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