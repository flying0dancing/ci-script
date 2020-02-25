/**
 * copy scripts for installing agile reporter and product installers
 * @param propertiesSet
 * @return
 */
def call(propertiesSet){
    shallowCheckout()
    def app_hostuser=propertiesSet['app.user']+'@'+propertiesSet['app.host']
    def downloadPath='/home/'+propertiesSet['app.user']+'/'
    //sh( returnStatus: true, script: '''scp '''+env.WORKSPACE+'''/scripts/Remote*.sh '''+app_hostuser+''':'''+downloadPath)
    //sh(returnStdout: true, script: '''ssh '''+app_hostuser+''' 'chmod u+x Remote*.sh ' ''')

    sshagent(credentials: ['product-ci-sha-local1-user-test']) {
        hello('girl')
        def flag1=sh( returnStatus: true, script: "scp -o StrictHostKeyChecking=no ${env.WORKSPACE}/scripts/Remote*.sh $app_hostuser:$downloadPath")
        def flag2=sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $app_hostuser 'chmod u+x Remote*.sh ' ")
        echo "flag1=$flag1"
        echo "flag2=$flag2"
    }
}