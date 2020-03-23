def call(props){
    def startSh=props['app.install.path']+'/bin/start.sh'
    def selectedEnv=envVars.get(props)
    def app_hostuser=selectedEnv.host
    def flag
    sshagent(credentials: [selectedEnv.credentials]) {
        flag=sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser 'sh ${startSh} --force' ")
        echo "start result:$flag"
    }
    return flag
}