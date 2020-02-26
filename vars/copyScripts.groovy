/**
 * copy scripts for installing agile reporter and product installers
 * @param propertiesSet
 * @return
 */
def call(propertiesSet){
    shallowCheckout('sshTest')

    def envLabel=propertiesSet['app.user']+'-'+propertiesSet['app.host']
    echo "envLabel $envLabel"
    def selectedEnv=envVars.get(envLabel)
    envVars.check(envLabel,selectedEnv)
    def app_hostuser=selectedEnv.host
    def downloadPath=selectedEnv.homeDir

    sshagent(credentials: [selectedEnv.credentials]) {
        sh( returnStatus: true, script: "scp -o StrictHostKeyChecking=no ${env.WORKSPACE}/scripts/Remote*.sh $app_hostuser:$downloadPath")
        sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser 'chmod u+x Remote*.sh ' ")
    }
    def dbLabel=propertiesSet['database.user']+'-'+propertiesSet['database.host']
    def selectedDB=envVars.get(dbLabel)
    envVars.check(dbLabel,selectedDB)
    def dbserver_hostuser=selectedDB.host
    downloadPath=selectedDB.homeDir
    sshagent(credentials: [selectedDB.credentials]) {
        def flag1=sh( returnStatus: true, script: "scp -o StrictHostKeyChecking=no -r ${env.WORKSPACE}/scripts/${selectedDB.configDir} $dbserver_hostuser:$downloadPath")
        echo "flag:$flag1"
        flag1=sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $dbserver_hostuser 'chmod u+x ${selectedDB.configDir}*.sh ' ")
        echo "flag:$flag1"
    }
    sshagent(credentials: ['product-ci-sha-db1-user-oracle']) {
        def flag1=sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no oracle@sha-oracle-01 'hostname' ")
        echo "oracle flag:$flag1"
    }
    sshagent(credentials: ['product-ci-sha-local2-user-test']) {
        def flag1=sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no test@172.20.30.89 'hostname' ")
        echo "prod test flag:$flag1"
    }

}