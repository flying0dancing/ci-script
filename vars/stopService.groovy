def call(props){
    def startSh=props['app.install.path']+'/bin/stop.sh'
    def selectedEnv=envVars.get(props)
    def app_hostuser=selectedEnv.host
    def flag
    echo "stop service......"
    sshagent(credentials: [selectedEnv.credentials]) {
        sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser 'chmod u+x ${props['app.install.path']}/bin/*.sh ' ")
        flag=sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser 'nohup ${startSh} >> ${selectedEnv.homeDir}/nohup.out 2>&1 &' ")
        echo "stop service result:$flag"
    }
    return flag
}