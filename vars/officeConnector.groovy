def call(props,urlHook='https://outlook.office.com/webhook/faaeef7d-d836-45a5-9b07-0f1f13d0c25b@4a78f2c0-297f-426d-b09f-5986924d38e7/IncomingWebhook/a33daad345b74ad29ecc77c000f42df3/ab2c9d48-0305-4d56-9959-8c7215573a5a'){
    def mBody =sendContent(props)
    office365ConnectorSend message: "$mBody", status:"${currentBuild.currentResult}", webhookUrl:"$urlHook"
}


def sendContent(props){
    def appUrl=readProperty.getAppURL(props)
    def mBody ="run on node ${NODE_NAME}, "
    mBody += "used ${props['default.use.repo']} repo, "
    mBody += "deploy on ${props['app.user']}@${props['app.host']}, installed at ${props['app.install.path']}\n"
    mBody += "(<${appUrl}|Open>)"

    return mBody
}