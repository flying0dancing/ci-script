def call(props){
    def mTo=props['mail.receiver.toList']
    def mCc=props['mail.receiver.ccList']
    def mSubject="Job '${JOB_NAME}' (${BUILD_NUMBER}) is done"
    def mBody = "<!DOCTYPE html>"
    mBody += "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"
    mBody += "<title>product qa deploy</title>"
    mBody += "</head><body>"
    mBody += "hi,<br/><br/>"
    mBody += "<p>This email is send by automatic system, please don't reply.</p>"
    mBody += "run on node ${NODE_NAME}<br/>"
    mBody += "build ${BUILD_DISPLAY_NAME}: <a href=\"${BUILD_URL}\">${BUILD_URL}</a><br/>"
    mBody += "used ${props['default.use.repo']} repo<br/>"
    mBody += "deploy on ${props['app.user']}@${props['app.host']}<br/>"
    mBody += "deploy path ${props['app.install.path']}<br/>"
    mBody += "result: ${currentBuild.currentResult}<br/>"
    mBody += "<p>${currentBuild.description}</p>"
    mBody += "<br/><br/>Thanks.</div>"
    mBody += "</body></html>"
    mail from: '', replyTo: '', to: mTo, cc: mCc, bcc: '', subject: mSubject, body: mBody
}