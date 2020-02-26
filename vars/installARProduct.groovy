/**
 * install agile reporter product package, if result in RemoteInstall_1.tmp contains fail, it will terminate.
 * @param projectName: like hkma, mas...
 * @param propertiesSet: get value from deploy folder's env.properties
 * @param installerFullName: full name in local server
 * @return
 */
def call(projectName,propertiesSet,installerFullName){

    def envLabel=propertiesSet['app.user']+'-'+propertiesSet['app.host']
    def selectedEnv=envVars.get(envLabel)
    def app_hostuser=selectedEnv.host
    def ocelotPath=propertiesSet['app.install.path']

    sshagent(credentials: [selectedEnv.credentials]) {
        def allstatus=sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser  'sh RemoteInstall.sh -help' ")
        echo 'sh RemoteInstall.sh '+ocelotPath+' 1 '+installerFullName
        //sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser  'sh RemoteInstall.sh $ocelotPath 1 $installerFullName ' ")
        def stepInfo='install or upgrade product '
        if(allstatus==0){
            createHtmlContent('stepline',stepInfo+'pass.')
            echo stepInfo+'pass.'
        }else{
            createHtmlContent('stepline',stepInfo+'contains fail.')
            createHtmlContent('stepEndFlag')
            error stepInfo+'contains fail.'
        }
    }
}