def call(props){
    def mBody ="run on node ${NODE_NAME},"
    mBody += "used ${props['default.use.repo']} repo,\t"
    mBody += "deploy on ${props['app.user']}@${props['app.host']}, installed at ${props['app.install.path']}"
    //mBody += "${currentBuild.description}\n"
    def urllink='https://outlook.office.com/webhook/faaeef7d-d836-45a5-9b07-0f1f13d0c25b@4a78f2c0-297f-426d-b09f-5986924d38e7/JenkinsCI/d401f7a935f04ac4aa2133fd779c749f/d23eb2ed-8493-4c84-b7f2-36fee3c142c8'
    office365ConnectorSend message: "$mBody", status:"${currentBuild.currentResult}", webhookUrl:"$urllink"
}

def connectNewHook(props,urllink){
    def mBody ="run on node ${NODE_NAME},"
    mBody += "used ${props['default.use.repo']} repo,\t"
    mBody += "deploy on ${props['app.user']}@${props['app.host']}, installed at ${props['app.install.path']}"
    office365ConnectorSend message: "$mBody", status:"${currentBuild.currentResult}", webhookUrl:"$urllink"

}