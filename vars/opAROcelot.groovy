/**
 * new install or upgrade agile reporter, use ocelot.user.password set to null to new install, others upgrade
 * @param projectName: like hkma, mas...
 * @param propertiesSet: get value from deploy folder's env.properties
 * @param installerFullName: full name in local server
 * @param ocelotPropFileName: ocelot properties's name like 'hkma_upgrade_ocelot.properties' 'hkma_new_ocelot.properties'
 * @return
 */
def call(projectName,propertiesSet,installerFullName,ocelotPropFileName){

    def envLabel=propertiesSet['app.user']+'-'+propertiesSet['app.host']
    def selectedEnv=envVars.get(envLabel)
    def app_hostuser=selectedEnv.host
    def ocelotPath=propertiesSet['app.install.path']
    def downloadPath=ocelotPath+'/deploys/'

    //transfer ocelot properties to workspace for getting properties
    def ocelotPropFilePath=env.WORKSPACE+'/'+projectName+'/src/main/resources/properties/'
    def ocelotProps=readProperties file: ocelotPropFilePath+ocelotPropFileName
    def ocleot_user_password=ocelotProps['ocelot.user.password']

    if(ocleot_user_password){
        createHtmlContent('stepline','new install')
        echo "new install agile reporter platform"
        def dbLabel=propertiesSet['database.user']+'-'+propertiesSet['database.host']
        def selectedDB=envVars.get(dbLabel)
        envVars.check(dbLabel,selectedDB)
        def dbserver_hostuser=selectedDB.host
        def local_dbname=ocelotProps['ocelot.jdbc.username']
        def local_dbinstance=ocelotProps['ocelot.jdbc.url']
        def jdbcurlarr=local_dbinstance.split(':')
        local_dbinstance=jdbcurlarr[-1]
        echo "create oracle database"
        createHtmlContent('stepline','create oracle database')
        sshagent(credentials: [selectedDB.credentials]) {
            sh( returnStatus: true, script: "scp -o StrictHostKeyChecking=no -r ${env.WORKSPACE}/scripts/${selectedDB.configDir} $dbserver_hostuser:${selectedDB.homeDir}")
            sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $dbserver_hostuser 'chmod u+x ${selectedDB.configDir}*.sh ' ")
            sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $dbserver_hostuser  'sh ${selectedDB.configDir}opSchema.sh $local_dbinstance $local_dbname'")
        }
        createHtmlContent('stepline','create ocelot install path')
        echo "create ocelot install path"
        sshagent(credentials: [selectedEnv.credentials]) {
            sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser  'rm -rf $ocelotPath && mkdir $ocelotPath' ")
        }
    }else{
        echo "upgrade agile reporter platform"
        createHtmlContent('stepline','upgrade install')
    }

    sshagent(credentials: [selectedEnv.credentials]) {
        sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser  'mkdir $downloadPath 2>/dev/null' ")
        //copy ocelot.properties to local ocelot folder
        def stepInfo='find and copy '+ocelotPropFileName
        def flag=sh( returnStatus: true, script: "scp -o StrictHostKeyChecking=no `find ${env.WORKSPACE}/$projectName/src/main/resources/properties/ -type f -name \"$ocelotPropFileName\"` $app_hostuser:$downloadPath")
        if(flag==0){
            createHtmlContent('stepline',stepInfo+' pass.')

            def allstatus=sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser  'sh RemoteInstall.sh -help' ")
            echo "sh RemoteInstall.sh $ocelotPath 0 $installerFullName $downloadPath$ocelotPropFileName"
            //sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser  'sh RemoteInstall.sh $ocelotPath 0 $installerFullName $downloadPath$ocelotPropFileName ' ")
            stepInfo='install or upgrade ocelot '
            if(allstatus==0){
                createHtmlContent('stepline',stepInfo+'pass.')
                echo stepInfo+'pass.'
            }else{
                createHtmlContent('stepline',stepInfo+'contains fail.')
                createHtmlContent('stepEndFlag')
                error stepInfo+'contains fail.'
            }
        }else{
            createHtmlContent('stepline',stepInfo+' fail.')
            createHtmlContent('stepEndFlag')
            error "fail to copy properties file from slave"
        }
    }
}