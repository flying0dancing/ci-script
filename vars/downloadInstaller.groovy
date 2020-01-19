import static com.lombardrisk.pipeline.Credentials.AWS

void downloadOcelot(props,downloadFileName,downloadPath){
    def s3_ar_repo='AgileREPORTER/Releases/CandidateReleases/'
    downloadFromS3(s3_ar_repo,props,downloadFileName,downloadPath)
}

void downloadARProduct(projectFolder,props,downloadFileName,downloadPath){
    def s3_ar_repo='arproduct/'+projectFolder+'/CandidateReleases/'
    downloadFromS3(s3_ar_repo,props,downloadFileName,downloadPath)
}

/**download installer from s3
 * @s3repo: test.properties, get property s3.bucket, local.linux from it
 * @projectFolder like hkma, mas
 * @propertiesFileFullName: test.properties, get property s3.bucket, local.linux from it
 * @downloadFileName like CE_DPB_v1.0.0-b9_sign.lrm under <projectFolder>/candidate-release/<productVersionFolder>/
 * @downloadPath: local path, download in local.linux
 */
void downloadFromS3(s3repo,props,downloadFileName,downloadPath){
    def downfiles
    def local_linux=props['app.host']
    def app_user=props['app.user']
    def app_hostuser=app_user+'@'+local_linux
    def s3_bucket=props['s3.bucket']
    withAWS(credentials: 'aws') {
        downfiles=s3FindFiles(bucket:s3_bucket, path:s3repo, glob:"**/${downloadFileName}")
        //downfiles.each{print "${it.name},${it.path},${it.length},${it.lastModified}"}
    }
    if(downfiles){
        def lastIndex=0
        //withAWS(credentials: 'aws') {
        //    s3Download(bucket:s3_bucket, path:s3repo+downfiles[lastIndex].path,file:downfiles[lastIndex].path,force:true)
        //}
        String cmd = "s3 cp s3://$s3_bucket/$s3repo${downfiles[lastIndex].path} ${downfiles[lastIndex].path}  --no-progress "
        execute(cmd)
        def status=sh( returnStatus: true, script: 'scp '+downfiles[lastIndex].path+' '+app_hostuser+':'+downloadPath)
        echo "downloaded Agile Reporter Product completely."

    }else{
        error "there is no packages existed in bucket server, name like ${downloadFileName}"
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