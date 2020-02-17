/**
 * install agile reporter product package, if result in RemoteInstall_1.tmp contains fail, it will terminate.
 * @param projectName: like hkma, mas...
 * @param propertiesSet: get value from deploy folder's env.properties
 * @param installerFullName: full name in local server
 * @return
 */
def call(projectName,propertiesSet,installerFullName){

    def app_hostuser=propertiesSet['app.user']+'@'+propertiesSet['app.user']
    def ocelotPath=propertiesSet['app.install.path']

    def allstatus=sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteInstall.sh -help' ''')
    //sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteInstall.sh '''+ocelotPath+''' 1 '''+installerFullName+''' ' ''')
    //sh(returnStdout: true, script: '''ssh '''+app_hostuser+''' 'cat '''+ocelotPath+'''/RemoteInstall_1.tmp ' ''').trim()
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