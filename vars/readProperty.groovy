/*
String getARProductRepo(String productName){
    def repo='arproduct/'+productName+'/CandidateReleases/'
    if(downloadFromLocal()){
        repo=productLocalRepo
    }
    return repo
}

String getARRepo(){
    String repo='AgileREPORTER/Releases/CandidateReleases/'
    if(downloadFromLocal()){
        repo=arLocalRepo
    }
    return repo
}


Boolean downloadFromLocal(props){
    Boolean flag=false
    if(!defaultUseRepo || defaultUseRepo.equalsIgnoreCase('local')){
        flag=true
    }
    return flag
}

def getAppHostAndUser(){
    return appUser+'@'+appHost
}*/
//final String defaultUseRepo=getSomeProperties(propertiesFileFullName,'database.driver')
//final String defaultUseRepo=getSomeProperties(propertiesFileFullName,'database.host')
//final String defaultUseRepo=getSomeProperties(propertiesFileFullName,'database.user')
//final String defaultUseRepo=getSomeProperties(propertiesFileFullName,'database.backup.path')

//final String appInstallPath=getSomeProperties(propertiesFileFullName,'app.install.path')


//final String arLocalRepo=getSomeProperties(propertiesFileFullName,'ar.local.repo')
//final String productLocalRepo=getSomeProperties(propertiesFileFullName,'product.local.repo')
//final String s3Bucket=getSomeProperties(propertiesFileFullName,'s3.bucket')
//final String defaultUseRepo=getSomeProperties(propertiesFileFullName,'default.use.repo')

Boolean downloadFromLocal(props){
    Boolean flag=false
    def defaultUseRepo=props['default.use.repo']
    echo "${defaultUseRepo}"
    if(!defaultUseRepo || defaultUseRepo.equalsIgnoreCase('local')){
        flag=true
    }
    return flag
}