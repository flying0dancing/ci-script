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
    sshagent(credentials: [selectedEnv.credentials]) {
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
        echo "prod test flag11:$flag1"
        flag1=sh( returnStatus: true, script: "scp -o StrictHostKeyChecking=no ${env.WORKSPACE}/scripts/Remote*.sh test@172.20.30.89:/home/test")
        echo "prod test flag12:$flag1"
    }
    sshagent(credentials: ['product-ci-sha-local2-user-test']) {
        def flag2=sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no test@sha-prod-001 'hostname' ")
        echo "prod test flag21:$flag2"
        flag2=sh( returnStatus: true, script: "scp -o StrictHostKeyChecking=no ${env.WORKSPACE}/scripts/RemoteProduct*.sh test@sha-prod-001:/home/test")
        echo "prod test flag22:$flag2"
    }
    sshagent(credentials: ['product-ci-sha-local1-user-test']) {
        def flag1=sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no test@172.20.31.7 'hostname' ")
        echo "prod test flag31:$flag1"
        flag1=sh( returnStatus: true, script: "scp -o StrictHostKeyChecking=no ${env.WORKSPACE}/vars/hello.groovy test@172.20.31.7:/home/test")
        echo "prod test flag32:$flag1"
    }
    sshagent(credentials: ['product-ci-sha-local1-user-test']) {
        def flag2=sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no test@sha-com-qa-3 'hostname' ")
        echo "prod test flag41:$flag2"
        flag2=sh( returnStatus: true, script: "scp -o StrictHostKeyChecking=no ${env.WORKSPACE}/vars/helper.groovy test@sha-com-qa-3:/home/test")
        echo "prod test flag42:$flag2"
    }

}