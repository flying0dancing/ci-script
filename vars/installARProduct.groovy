/**
 * install agile reporter product package, if result in RemoteInstall_1.tmp contains fail, it will terminate.
 * @param projectName: like hkma, mas...
 * @param propertiesSet: get value from deploy folder's env.properties
 * @param installerFullName: full name if already in local server, installerName if in S3
 * @param installerName
 * @return
 */
def call(projectName,propertiesSet,installerFullName,installerName){

    def app_hostuser=propertiesSet['app.user']+'@'+propertiesSet['app.user']
    def ocelotPath=propertiesSet['app.install.path']

    def stepInfo='download product package'
    if(installerFullName.contains('/')){
        //download from local server
        createHtmlContent('stepline',stepInfo+' from local')
    }else{
        createHtmlContent('stepline',stepInfo+' from remote')
        installerFullName=downloadInstaller.downloadARProduct(projectName,propertiesSet,installerName)
    }
    sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteInstall.sh -help' ''')
    //sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteInstall.sh '''+ocelotPath+''' 1 '''+installerFullName+''' ' ''')
    def allstatus=sh(returnStdout: true, script: '''ssh '''+app_hostuser+''' 'cat '''+ocelotPath+'''/RemoteInstall_1.tmp ' ''').trim()
    if(allstatus){
        createHtmlContent('stepline',allstatus)
        if(allstatus.contains('fail')){
            createHtmlContent('stepEndFlag')
            error "install or upgrade product contains fail."
        }else{
            echo "install or upgrade product pass."
        }
    }
}