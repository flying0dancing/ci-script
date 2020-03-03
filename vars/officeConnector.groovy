def call(props){
    def mBody ="run on node ${NODE_NAME}, "
    mBody += "used ${props['default.use.repo']} repo, "
    mBody += "deploy on ${props['app.user']}@${props['app.host']}, installed at ${props['app.install.path']}"
    //mBody += "${currentBuild.description}\n"
    //def urlHook='https://outlook.office.com/webhook/faaeef7d-d836-45a5-9b07-0f1f13d0c25b@4a78f2c0-297f-426d-b09f-5986924d38e7/JenkinsCI/d401f7a935f04ac4aa2133fd779c749f/d23eb2ed-8493-4c84-b7f2-36fee3c142c8' //QA_Deploy, outlook can receive it, teams cannot.
    def urlHook='https://outlook.office.com/webhook/faaeef7d-d836-45a5-9b07-0f1f13d0c25b@4a78f2c0-297f-426d-b09f-5986924d38e7/IncomingWebhook/a33daad345b74ad29ecc77c000f42df3/ab2c9d48-0305-4d56-9959-8c7215573a5a'
    office365ConnectorSend message: "$mBody", status:"${currentBuild.currentResult}", webhookUrl:"$urlHook"
}

def connectNewHook(props,urlHook){
    def mBody ="run on node ${NODE_NAME}, "
    mBody += "used ${props['default.use.repo']} repo, "
    mBody += "deploy on ${props['app.user']}@${props['app.host']}, installed at ${props['app.install.path']}"
    office365ConnectorSend message: "$mBody", status:"${currentBuild.currentResult}", webhookUrl:"$urlHook"

}