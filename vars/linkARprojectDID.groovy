/***
 *config DID part, arguments: projectName, productPrefix, DID's properties's name suffix like 'aliasinfo.properties'
 * @projectFolder like hkma, mas
 * @propertiesFileFullName: test.properties, get property local.linux local.oracle ar.repo.linux from it
 * @productPrefix like CE_DPB_v1.0.0-b9_sign.lrm's CE_DPB
 * @productVersion like CE_DPB_v1.0.0-b9_sign.lrm's 1.0.0-b9
 * @productPropFileName: ce config properties
 * @productPropAliases: like "STB Work:STB System:STB System HKMA"
 * @eaFlag: 1 means config argument is -ea, 2 means config argument is -da and -aa
 */
def call(projectFolder,propertiesSet,productPrefix,productVersion,productPropFileName,productPropAliases,eaFlag){
    def continue_status1='RemoteInstall_'+eaFlag+'.tmp'
    def local_linux=propertiesSet['app.host']
    def app_user=propertiesSet['app.user']
    def app_hostuser=app_user+'@'+local_linux
    def ocelotPath=propertiesSet['app.install.path']
    def downloadPath=ocelotPath+'/bin/'

    //copy aliasinfo.properties to local ocelot folder
    stepInfo='find and copy '+productPropFileName
    flag=sh( returnStatus: true, script: '''scp `find '''+env.WORKSPACE+'''/'''+projectFolder+'''/src/main/resources/properties/ -type f -name "'''+productPropFileName+'''"` '''+app_hostuser+''':'''+downloadPath)
    if(flag==0){
        createHtmlContent('stepline',stepInfo+' pass')
        sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteInstall.sh '''+ocelotPath+''' '''+eaFlag+''' '''+productPropFileName+''' '''+productPrefix.toUpperCase()+''' '''+ productVersion+''' \"'''+productPropAliases+'''\" ' ''')
        def allstatus=sh(returnStdout: true, script: '''ssh '''+app_hostuser+''' 'cat '''+ocelotPath+'''/'''+continue_status1+''' ' ''').trim()
        if(allstatus){
            createHtmlContent('stepline','config DID: '+allstatus.replaceAll('configure','<br />configure'))
            if(allstatus.contains('fail')){
                createHtmlContent('stepEndFlag')
                error "config properties contains fail."
            }else{
                echo "config properties pass."
            }
        }
    }else{
        createHtmlContent('stepline',stepInfo+' fail')
        createHtmlContent('stepEndFlag')
        error "fail to copy properties file from slave"
    }
}