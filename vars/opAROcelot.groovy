/***
 *new install or upgrade ar platform part, use @ocelotPropFileName ocelot.user.password= to judged new install or upgrade
 * @projectFolder like hkma, mas
 * @propertiesFileFullName: env.properties, get property app.host app.user default.use.repo database.driver database.host database.user ar.local.repo from it
 * @installerName: like AgileREPORTER-19.3.0-b207.jar
 * @ocelotPropFileName: ocelot properties's name like 'hkma_upgrade_ocelot.properties' 'hkma_new_ocelot.properties'
 */
def call(projectFolder,propertiesSet,installerFullName,installerName,ocelotPropFileName){
    def local_linux=propertiesSet['app.host']
    def app_user=propertiesSet['app.user']

    def app_hostuser=app_user+'@'+local_linux
    def ocelotPath=propertiesSet['app.install.path']
    def downloadPath=ocelotPath+'/deploys/'

    //transfer ocelot properties to workspace for getting properties
    def ocelotPropFilePath=env.WORKSPACE+'/'+projectFolder+'/src/main/resources/properties/'
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
    def flag=sh( returnStatus: true, script: '''scp `find '''+env.WORKSPACE+'''/'''+projectFolder+'''/src/main/resources/properties/ -type f -name "'''+ocelotPropFileName+'''"` '''+app_hostuser+''':'''+downloadPath)
    if(flag==0){
        createHtmlContent('stepline',stepInfo+' pass')
        stepInfo='download ocelot '
        if(installerFullName && installerFullName.contains('/')){
            createHtmlContent('stepline',stepInfo+'from local')
        }else{
            //download from remote server
            createHtmlContent('stepline',stepInfo+'from remote')
            downloadInstaller.downloadOcelot(propertiesSet,installerName,downloadPath)
            installerFullName=downloadPath+installerName
        }
        sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteInstall.sh -help' ''')
        //sh( returnStatus: true, script: '''ssh '''+app_hostuser+'''  'sh RemoteInstall.sh '''+ocelotPath+''' 0 '''+installerFullName+''' '''+downloadPath+ocelotPropFileName+''' ' ''')
        def allstatus=sh(returnStdout: true, script: '''ssh '''+app_hostuser+''' 'cat '''+ocelotPath+'''/RemoteInstall_0.tmp ' ''').trim()
        if(allstatus){
            createHtmlContent('stepline',allstatus)
            if(allstatus.contains('fail')){
                createHtmlContent('stepEndFlag')
                error "install or upgrade ocelot contains fail."
            }else{
                echo "install or upgrade ocelot pass."
            }
        }
    }else{
        createHtmlContent('stepline',stepInfo+' fail')
        createHtmlContent('stepEndFlag')
        error "fail to copy properties file from slave"
    }
}