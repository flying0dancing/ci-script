/**
 * new install or upgrade agile reporter, use ocelot.user.password set to null to new install, others upgrade
 * @param projectName: like hkma, mas...
 * @param propertiesSet: get value from deploy folder's env.properties
 * @param installerFullName: full name if already in local server, installerName if in S3
 * @param installerName
 * @param ocelotPropFileName: ocelot properties's name like 'hkma_upgrade_ocelot.properties' 'hkma_new_ocelot.properties'
 * @return
 */
def call(projectName,propertiesSet,installerFullName,installerName,ocelotPropFileName){

    def app_hostuser=propertiesSet['app.user']+'@'+propertiesSet['app.host']
    def ocelotPath=propertiesSet['app.install.path']
    def downloadPath=ocelotPath+'/deploys/'

    //transfer ocelot properties to workspace for getting properties
    def ocelotPropFilePath=env.WORKSPACE+'/'+projectName+'/src/main/resources/properties/'
    def ocelotProps=readProperties file: ocelotPropFilePath+ocelotPropFileName
    def ocleot_user_password=ocelotProps['ocelot.user.password']
    //def ocelotPath=getAROcelotProperty(ocelotPropFileName,'ocelot.install.path')
    sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'mkdir '''+downloadPath+''' 2>/dev/null ' ''')
    if(ocleot_user_password){
        createHtmlContent('stepline','new install')
        echo "new install agile reporter platform"
        def dbserver_hostuser=propertiesSet['database.user']+'@'+propertiesSet['database.host']
        def local_dbname=ocelotProps['ocelot.jdbc.username']
        def local_dbinstance=ocelotProps['ocelot.jdbc.url']
        def jdbcurlarr=local_dbinstance.split(':')
        local_dbinstance=jdbcurlarr[-1]
        echo "create oracle database"
        createHtmlContent('stepline','create oracle database')
        sh( returnStatus: true, script: '''ssh '''+dbserver_hostuser+'''  './opSchema.sh '''+local_dbinstance+''' '''+local_dbname+''' ' ''')

        createHtmlContent('stepline','create ocelot install path')
        echo "create ocelot install path"
        sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'rm -rf '''+ocelotPath+''' ' ''')
        sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'mkdir '''+ocelotPath+''' ' ''')
    }else{
        echo "upgrade agile reporter platform"
        createHtmlContent('stepline','upgrade install')
    }


    //copy ocelot.properties to local ocelot folder
    def stepInfo='find and copy '+ocelotPropFileName
    def flag=sh( returnStatus: true, script: '''scp `find '''+env.WORKSPACE+'''/'''+projectName+'''/src/main/resources/properties/ -type f -name "'''+ocelotPropFileName+'''"` '''+app_hostuser+''':'''+downloadPath)
    if(flag==0){
        createHtmlContent('stepline',stepInfo+' pass')
        stepInfo='download ocelot '
        if(installerFullName.contains('/')){
            createHtmlContent('stepline',stepInfo+'from local')
        }else{
            //download from remote server
            createHtmlContent('stepline',stepInfo+'from remote')
            installerFullName=downloadInstaller.downloadOcelot(propertiesSet,installerName)
        }
        def allstatus=sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteInstall.sh -help' ''')
        //sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteInstall.sh '''+ocelotPath+''' 0 '''+installerFullName+''' '''+downloadPath+ocelotPropFileName+''' ' ''')
        //sh(returnStdout: true, script: '''ssh '''+app_hostuser+''' 'cat '''+ocelotPath+'''/RemoteInstall_0.tmp ' ''').trim()

        if(allstatus==0){
            createHtmlContent('stepline',"install or upgrade ocelot pass.")
            echo "install or upgrade ocelot pass."
        }else{
            createHtmlContent('stepline',"install or upgrade ocelot contains fail.")
            createHtmlContent('stepEndFlag')
            error "install or upgrade ocelot contains fail."
        }
    }else{
        createHtmlContent('stepline',stepInfo+' fail')
        createHtmlContent('stepEndFlag')
        error "fail to copy properties file from slave"
    }
}