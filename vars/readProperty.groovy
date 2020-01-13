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


Boolean downloadFromLocal(){
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
final def props=getSomeProperties(propFile)
final String appHost=props['app.host']
final String appUser=props['app.user']
//final String appInstallPath=getSomeProperties(propertiesFileFullName,'app.install.path')


//final String arLocalRepo=getSomeProperties(propertiesFileFullName,'ar.local.repo')
//final String productLocalRepo=getSomeProperties(propertiesFileFullName,'product.local.repo')
//final String s3Bucket=getSomeProperties(propertiesFileFullName,'s3.bucket')
//final String defaultUseRepo=getSomeProperties(propertiesFileFullName,'default.use.repo')


def getSomeProperties(String propFile){
    def props=new Properties();
    new File(propFile).withInputStream{stream->props.load(stream)}
    return props
}