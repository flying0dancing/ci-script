/**
 *
 * @param projectName: like hkma, mas...
 * @param propertiesSet: get value from deploy folder's env.properties
 * @param productPrefix：if in hkma project, it should be ce_hkma or ce_dpb or hkma or dpb
 * @param productVersion：like CE_DPB_v1.0.0-b9_sign.lrm's 1.0.0-b9
 * @param productProp: get value like config:HKMAoracleSystemaliasinfo.properties and aliases:"STB Work:STB System:STB System HKMA"
 * @param eaFlag: 1 means config argument is -ea, 2 means config argument is -da and -aa
 * @return
 */
def call(projectName,propertiesSet,productPrefix,productVersion,productProp,eaFlag){

    def app_hostuser=propertiesSet['app.user']+'@'+propertiesSet['app.host']
    def ocelotPath=propertiesSet['app.install.path']
    def downloadPath=ocelotPath+'/deploys/'
    sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'mkdir '''+downloadPath+''' 2>/dev/null ' ''')
    def propyFile=productProp.filename
    def propyAliases=productProp.aliases
    //copy aliasinfo.properties to local ocelot folder
    stepInfo='find and copy '+propyFile
    flag=sh( returnStatus: true, script: '''scp `find '''+env.WORKSPACE+'''/'''+projectName+'''/src/main/resources/properties/ -type f -name "'''+propyFile+'''"` '''+app_hostuser+''':'''+downloadPath)
    if(flag==0){
        createHtmlContent('stepline',stepInfo+' pass')
        sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteInstall.sh '''+ocelotPath+''' '''+eaFlag+''' '''+downloadPath+propyFile+''' '''+productPrefix.toUpperCase()+''' '''+ productVersion+''' \"'''+propyAliases+'''\" ' ''')
        def continue_status='RemoteInstall_'+eaFlag+'.tmp'
        def allstatus=sh(returnStdout: true, script: '''ssh '''+app_hostuser+''' 'cat '''+ocelotPath+'''/'''+continue_status+''' ' ''').trim()
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