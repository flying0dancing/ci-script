String searchLatestFromLocal(localRepo,props,productPrefixAndVersion,buildNumber,productSuffix){

    def app_hostuser=props['app.user']+'@'+props['app.host']
    def searchContent
    if(buildNumber){
        searchContent=productPrefixAndVersion+'*'+buildNumber+'*'+productSuffix
    }else{
        searchContent=productPrefixAndVersion+'*'+productSuffix
    }
    def flag=sh( returnStdout: true, script: '''ssh '''+app_hostuser+'''  'find '''+localRepo+''' -iname '''+searchContent+''' -print0|xargs -0 stat -c'%Y:%n'|sort -nr|cut -d ':' -f 2|head -n 1' ''')
    return flag
}

String searchLatestProduct(projectFolder,props,productPrefix,productVersion,buildNumber){
    def downloadFileName
    def repo
    if(readProperty.downloadFromLocal(props)){
        repo=props['product.local.repo']
        downloadFileName=searchLatestFromLocal(repo,props,productPrefix+'_v'+productVersion,buildNumber,'.lrm')
    }else{
        repo='arproduct/'+projectFolder+'/CandidateReleases/'
        downloadFileName=searchLatestFromS3(repo,props,productPrefix+'_v'+productVersion,buildNumber,'.lrm')
        if(!downloadFileName){
            repo='arproduct/'+productPrefix.toLowerCase()+'/CandidateReleases/'
            downloadFileName=searchLatestFromS3(repo,props,productPrefix+'_v'+productVersion,buildNumber,'.lrm')
        }
    }

    return downloadFileName
}

String searchLatestOcelot(props,productPrefix,productVersion,buildNumber){
    def downloadFileName
    def repo
    if(readProperty.downloadFromLocal(props)){
        repo=props['ar.local.repo']
        downloadFileName=searchLatestFromLocal(repo,props,productPrefix+'-'+productVersion,buildNumber,'.jar')
    }else{
        repo='AgileREPORTER/Releases/CandidateReleases/'
        downloadFileName=searchLatestFromS3(repo,props,productPrefix+'-'+productVersion,buildNumber,'.jar')
    }

    return downloadFileName
}

/**search installer from s3
 * @s3repo: test.properties, get property s3.bucket, local.linux from it
 * @projectFolder like hkma, mas
 * @propertiesFileFullName: test.properties, get property s3.bucket, local.linux from it
 * @productPrefixAndVersion like CE_DPB_v1.0.0-b9_sign.lrm's CE_DPB_v1.0.0
 * @buildNumber: like CE_DPB_v1.0.0-b9_sign.lrm's b9
 * @productSuffix: .lrm .jar
 */
String searchLatestFromS3(s3repo,props,productPrefixAndVersion,buildNumber,productSuffix){
    def downloadFileName
    def downfiles
    def s3_bucket=props['s3.bucket']
    withAWS(credentials: 'aws') {
        if(buildNumber){
            downfiles=s3FindFiles(bucket:s3_bucket, path:s3repo, glob:"**/${productPrefixAndVersion}*${buildNumber}*${productSuffix}")
        }else{
            downfiles=s3FindFiles(bucket:s3_bucket, path:s3repo, glob:"**/${productPrefixAndVersion}*${productSuffix}")
        }

    }
    if(downfiles){
        //downfiles.each{print "${it.name},${it.path},${it.length},${it.lastModified}"}
        def lastIndex=0
        def lastBuildNumber=downfiles[0].lastModified
        downloadFileName=downfiles[0].name
        for(int index=0;index<downfiles.size();index++){
            if(lastBuildNumber<downfiles[index].lastModified){
                lastBuildNumber=downfiles[index].lastModified
                lastIndex=index
                downloadFileName=downfiles[index].name
            }
        }
    }
    echo "Latest installer name "+downloadFileName
    return downloadFileName
}