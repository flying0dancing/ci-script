/**
 * copy scripts for installing agile reporter and product installers
 * @param propertiesSet
 * @return
 */
def call(propertiesSet){
    //def envLabel=propertiesSet['app.user']+'-'+propertiesSet['app.host']
    def selectedEnv=envVars.get(propertiesSet)
    def app_hostuser=selectedEnv.host
    def downloadPath=selectedEnv.homeDir

    sshagent(credentials: [selectedEnv.credentials]) {
        sh( returnStatus: true, script: "scp -o StrictHostKeyChecking=no ${env.WORKSPACE}/scripts/Remote*.sh $app_hostuser:$downloadPath")
        sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser 'chmod u+x Remote*.sh ' ")
    }

}