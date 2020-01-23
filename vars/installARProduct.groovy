/***
 *install arproduct packages part, if RemoteInstall_0.tmp contains fail, it will terminate.
 * @projectFolder like hkma, mas
 * @propertiesFileFullName: test.properties, get property local.linux local.oracle ar.repo.linux from it
 * @downloadFileName like CE_DPB_v1.0.0-b9_sign.lrm under <projectFolder>/candidate-release/<productVersionFolder>/
 * @downloadFromLocalServer: null means download from remote
 * @ocelotPath: install path
 */
def call(projectFolder,propertiesSet,installerFullName,installerName){
    def local_linux=propertiesSet['app.host']
    def app_user=propertiesSet['app.user']

    def stepInfo='download product package'
    def app_hostuser=app_user+'@'+local_linux
    def ocelotPath=propertiesSet['app.install.path']
    def downloadPath=ocelotPath+'/deploys/'

    //create download folder deploys
    sh( returnStatus: true, script: '''ssh '''+app_hostuser+''' 'mkdir '''+downloadPath+''' 2>/dev/null ' ''')
    if(installerFullName && installerFullName.contains('/')){
        //download from local server
        createHtmlContent('stepline',stepInfo+' from local')
    }else{
        createHtmlContent('stepline',stepInfo+' from local')
        downloadInstaller.downloadARProduct(projectFolder,propertiesSet,installerName,downloadPath)
        installerFullName=downloadPath+installerName
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