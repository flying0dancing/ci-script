Boolean downloadFromLocal(props){
    Boolean flag=false
    def defaultUseRepo=props['default.use.repo']
    //echo "deployment.properties==>default.use.repo=${defaultUseRepo}"
    if(defaultUseRepo && defaultUseRepo.equalsIgnoreCase('local')){
        flag=true
    }
    return flag
}

def get(projectFolder,deployFolder){
    def propertiesFileName='deployment.properties'
    def propertiesFiles=findFiles(glob: '**/'+projectFolder+'/**/'+deployFolder+'/'+propertiesFileName)
    def propertiesSet=readProperties file: propertiesFiles[0].path
    propertiesSet=helper.resetProps(propertiesSet)
    return propertiesSet
}

def getAppURL(props){
    def ocelotProperties=props['app.install.path']+'/ocelot.properties'
    def selectedEnv=envVars.get(props)
    def app_hostuser=selectedEnv.host
    def hostIp=app_hostuser[app_hostuser.indexOf('@')+1..-1]
    def appUrl
    sshagent(credentials: [selectedEnv.credentials]) {
        appUrl=sh( returnStdout: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser 'sh RemoteGetUrl.sh ${ocelotProperties}' ").trim()
        appUrl=appUrl.replaceAll('#',hostIp)
        echo "url:$appUrl"
    }
    return appUrl
}