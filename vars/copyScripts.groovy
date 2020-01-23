def call(propertiesSet){
    def app_hostuser=propertiesSet['app.user']+'@'+propertiesSet['app.host']
    def downloadPath='/home/'+propertiesSet['app.user']+'/'
    sh( returnStatus: true, script: '''scp scripts/Remote*.sh '''+app_hostuser+''':'''+downloadPath)
    sh(returnStdout: true, script: '''ssh '''+app_hostuser+''' 'chmod u+x Remote*.sh ' ''')
}