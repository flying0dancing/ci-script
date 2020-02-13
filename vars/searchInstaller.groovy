
String searchLatestProduct(projectName,props,productPrefix,productVersion,buildNumber){
    def downloadFileName
    def repo
    def nameSuffix='.lrm'
    def content=searchContent(productPrefix+'_v'+productVersion,buildNumber,nameSuffix)
    if(readProperty.downloadFromLocal(props)){
        repo=props['product.local.repo']
        downloadFileName=searchLatestFromLocal(repo,props,content)
    }else{
        repo='arproduct/'+projectName+'/CandidateReleases/'
        downloadFileName=searchLatestFromS3(repo,props,content)
        if(!downloadFileName){
            repo='arproduct/'+productPrefix.toLowerCase()+'/CandidateReleases/'
            downloadFileName=searchLatestFromS3(repo,props,content)
        }
    }

    return downloadFileName
}

String searchLatestOcelot(props,productPrefix,productVersion,buildNumber){
    def downloadFileName
    def repo
    def nameSuffix='.jar'
    def content=searchContent(productPrefix+'-'+productVersion,buildNumber,nameSuffix)
    if(readProperty.downloadFromLocal(props)){
        repo=props['ar.local.repo']
        downloadFileName=searchLatestFromLocal(repo,props,content)
    }else{
        repo='AgileREPORTER/Releases/CandidateReleases/'
        downloadFileName=searchLatestFromS3(repo,props,content)
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
 * @return
 */
String searchLatestFromS3(s3repo,props,searchContent){
    def downloadFileName
    def downfiles
    def s3_bucket=props['s3.bucket']
    withAWS(credentials: 'aws') {
        downfiles=s3FindFiles(bucket:s3_bucket, path:s3repo, glob:"**/${searchContent}")
    }
    if(downfiles){
        //downfiles.each{print "${it.name},${it.path},${it.length},${it.lastModified}"}
        def lastIndex=0
        def lastBuildNumber=downfiles[0].lastModified
        downloadFileName=downfiles[0].name
        //start
        def newestLastModified=downfiles.collect{return it.lastModified}.max()
        downloadFileName=downfiles.find{return itlastModified==newestLastModified}
        //end
        echo "Latest installer name Method2:"+downloadFileName
        for(int index=0;index<downfiles.size();index++){
            if(lastBuildNumber<downfiles[index].lastModified){
                lastBuildNumber=downfiles[index].lastModified
                lastIndex=index
                downloadFileName=downfiles[index].name
            }
        }
        echo "Latest installer name Method1:"+downloadFileName

    }
    echo "Latest installer name "+downloadFileName
    return downloadFileName
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
    def lastestFileFullname=sh( returnStdout: true, script: '''ssh '''+app_hostuser+'''  'find '''+localRepo+''' -iname '''+searchContent+''' -print0|xargs -0 stat -c'%Y:%n'|sort -nr|cut -d ':' -f 2|head -n 1' ''')
    return lastestFileFullname
}


/**
 * check need to install or not, return 0 means need, return others means no need
 * @param props
 * @param installerName
 * @return
 */
int remoteInstallercheck(props,installerName){
    def ocelotPath=props['app.install.path']
    def app_hostuser=props['app.user']+'@'+props['app.host']

    def flag=sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteProductInstallerCheck.sh '''+ocelotPath+''' '''+installerName+''' ' ''')
    return flag
}
