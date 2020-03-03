def call(props){
    def mTo=props['mail.receiver.toList']
    def mCc=props['mail.receiver.ccList']
    def mSubject="Job '${JOB_NAME}' (${BUILD_NUMBER}) is done"
    def mBody ="run on node ${NODE_NAME}\n"
    mBody += "build ${BUILD_DISPLAY_NAME}|${BUILD_URL}\n"
    mBody += "used ${props['default.use.repo']} repo\n"
    mBody += "deploy on ${props['app.user']}@${props['app.host']}\n"
    mBody += "deploy path ${props['app.install.path']}\n"
    mBody += "result: ${currentBuild.currentResult}\n"
    mBody += "${currentBuild.description}\n"

    def urllink='https://outlook.office.com/webhook/faaeef7d-d836-45a5-9b07-0f1f13d0c25b@4a78f2c0-297f-426d-b09f-5986924d38e7/JenkinsCI/d401f7a935f04ac4aa2133fd779c749f/d23eb2ed-8493-4c84-b7f2-36fee3c142c8'
    office365ConnectorSend message: "$mBody", status:"${currentBuild.currentResult}", webhookUrl:"$urllink"
}