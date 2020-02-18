import static com.lombardrisk.pipeline.Credentials.AWS

String searchLatestProduct(projectName,props,productPrefix,productVersion,buildNumber,remoteDownload=false){
    def downloadFileName
    def repo
    def nameSuffix='.lrm'
    def content=searchContent(productPrefix+'_v'+productVersion,buildNumber,nameSuffix)
    if(readProperty.downloadFromLocal(props)){
        repo='/home/'+props['app.user']+'/'+props['product.local.repo']
        downloadFileName=searchLatestFromLocal(repo,props,content)
    }else{
        repo='arproduct/'+projectName+'/CandidateReleases/'
        def homePath='/home/'+props['app.user']+'/'+props['product.local.repo']
        def local_repo=homePath+projectName+'/candidate-release/'
        downloadFileName=searchLatestFromS3(repo,props,content,local_repo,remoteDownload)

        if(!downloadFileName){
            productPrefix=productPrefix.toLowerCase()
            repo='arproduct/'+productPrefix+'/CandidateReleases/'
            local_repo=homePath+productPrefix+'/candidate-release/'
            downloadFileName=searchLatestFromS3(repo,props,content,local_repo,remoteDownload)
        }
    }
    return downloadFileName
}


String searchLatestOcelot(props,productPrefix,productVersion,buildNumber,remoteDownload=false){
    def downloadFileName
    def repo
    def nameSuffix='.jar'
    def content=searchContent(productPrefix+'-'+productVersion,buildNumber,nameSuffix)

    if(readProperty.downloadFromLocal(props)){
        repo='/home/'+props['app.user']+'/'+props['ar.local.repo']
        downloadFileName=searchLatestFromLocal(repo,props,content)
    }else{
        repo='AgileREPORTER/Releases/CandidateReleases/'
        def local_repo='/home/'+props['app.user']+'/'+props['ar.local.repo']
        downloadFileName=searchLatestFromS3(repo,props,content,local_repo,remoteDownload)
    }

    return downloadFileName
}

/**
 * combine a content for search
 * @param productPrefixAndVersion like CE_DPB_v1.0.0-b9_sign.lrm's CE_DPB_v1.0.0
 * @param buildNumber like CE_DPB_v1.0.0-b9_sign.lrm's b9
 * @param productSuffix like .lrm, .jar
 * @return
 */
String searchContent(productPrefixAndVersion,buildNumber,productSuffix){
    def searchContent
    if(buildNumber){
        searchContent=productPrefixAndVersion+'*'+buildNumber+'*'+productSuffix //like cd_dbp_v1.0.0*b9*.lrm
    }else{
        searchContent=productPrefixAndVersion+'*'+productSuffix //like CE_DPB_v*.lrm
    }
    return searchContent
}

/**
 * search installer name from s3
 * @param s3repo: like arproduct/hkma/CandidateReleases/ or AgileREPORTER/Releases/CandidateReleases/
 * @param props: get value from deploy folder's env.properties
 * @param searchContent: like cd_dbp_v1.0.0*b9*.lrm, CE_DPB_v*.lrm
 * @param local_repo: local server's path
 * @param downloadFlag: default is true, download; false is no need download
 * @return null or full local path
 */
String searchLatestFromS3(s3repo,props,searchContent,local_repo,downloadFlag=true){
    def sFilePath
    def sfiles
    def s3_bucket=props['s3.bucket']
    withAWS(credentials: 'aws') {
        sfiles=s3FindFiles(bucket:s3_bucket, path:s3repo, glob:"**/${searchContent}")
    }
    if(sfiles){
        //sfiles.each{print "${it.name},${it.path},${it.length},${it.lastModified}"}
        def newestLastModified=sfiles.collect{return it.lastModified}.max()
        sFilePath=sfiles.find{return it.lastModified==newestLastModified}
        echo "Latest installer path in s3: "+sFilePath
        if(downloadFlag){
            String cmd = "s3 cp s3://$s3_bucket/$s3repo$sFilePath $local_repo$sFilePath  --no-progress "
            execute(cmd)
            echo "download installer completely."
        }
        return local_repo+sFilePath
    }else{
        echo "there is no packages existed in bucket server, name like "+searchContent
    }
    return
}

/**
 * get latest file full name in local server, like repository/ARProduct/hkma/candidate-release/5.32.0/b96/CE_DPB_v5.32.0-b96_sign.lrm
 * @param localRepo: get value of ar.local.repo or product.local.repo in env.properties, like repository/ARProduct
 * @param props: get value from deploy folder's env.properties
 * @param searchContent: like cd_dbp_v1.0.0*b9*.lrm, CE_DPB_v*.lrm
 * @return
 */
String searchLatestFromLocal(localRepo,props,searchContent){

    def app_hostuser=props['app.user']+'@'+props['app.host']
    def flag=sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'find '''+localRepo+''' -iname '''+searchContent+''' -print0|xargs -0 stat -c'%Y:%n'|sort -nr|cut -d ':' -f 2|head -n 1' ''')
    def lastestFileFullname
    if(flag==0){
        lastestFileFullname=sh( returnStdout: true, script: '''ssh '''+app_hostuser+'''  'find '''+localRepo+''' -iname '''+searchContent+''' -print0|xargs -0 stat -c'%Y:%n'|sort -nr|cut -d ':' -f 2|head -n 1' ''').trim()
    }
    return lastestFileFullname
}


/**
 * check need to install or not, return 0 means need, return others means no need
 * @param props
 * @param installerName
 * @return
 */
int checkNeedInstallOrNot(props,installerName){
    def ocelotPath=props['app.install.path']
    def app_hostuser=props['app.user']+'@'+props['app.host']

    def flag=sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteProductInstallerCheck.sh '''+ocelotPath+''' '''+installerName+''' ' ''')
    return flag
}
/**
 * check installFullName exists or not, return 0 means exists, return others means no exists
 * @param props
 * @param installerFullName
 * @return
 */
int existsInLocal(props,installerFullName){
    def app_hostuser=props['app.user']+'@'+props['app.host']
    def flag=sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  '[ -f "'''+installerFullName+'''" ]' ''')
    return flag
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