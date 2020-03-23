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

int getAppURL(props){
    def ocelotProperties=props['app.install.path']+'/ocelot.properties'
    def selectedEnv=envVars.get(props)
    def app_hostuser=selectedEnv.host
    def hostPort
    def hostPortOffset
    def hostIp=app_hostuser[app_hostuser.indexOf('@')+1]
    echo "hostIp:$hostIp"
    def http="http://"
    def appUrl
    sshagent(credentials: [selectedEnv.credentials]) {
        hostIp=sh( returnStdout: true, script: '''ssh -o StrictHostKeyChecking=no $app_hostuser  'awk -F '=' /^main.host.name/'{print $"+"2}' \"${ocelotProperties}\"' ''').trim()
        hostPort=sh( returnStdout: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser  'awk -F '=' /^host.port[^.]/'{print \$"+"2}' \"${ocelotProperties}\"' ").trim()
        hostPortOffset=sh( returnStdout: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser  'awk -F '=' /^host.port.offset/'{print \$"+"2}' \"${ocelotProperties}\"' ").trim()
        httpsMode=sh( returnStdout: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser  'awk -F '=' /^httpsMode/'{print \$"+"2}' \"${ocelotProperties}\"' ").trim()
        if(httpsMode.equalsIgnoreCase('true')){
            http="https://"
        }
        echo "hostPort$hostPort"
        echo "hostPortOffset$hostPortOffset"
        hostPort=hostPort+hostPortOffset
        echo "add$hostPort"
        appUrl=http+hostIp+':'+hostPort
        echo "url:$appUrl"
    }
    return appUrl
}