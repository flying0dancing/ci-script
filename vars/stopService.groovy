def call(props){
    def startSh=props['app.install.path']+'/bin/stop.sh'
    def selectedEnv=envVars.get(props)
    def app_hostuser=selectedEnv.host
    def flag
    echo "stop service......"
    sshagent(credentials: [selectedEnv.credentials]) {
        flag=sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser 'nohup ${startSh} >> ${selectedEnv.homeDir}/nohup.out 2>&1 &' ")
        echo "start result:$flag"
    }
    return flag
}