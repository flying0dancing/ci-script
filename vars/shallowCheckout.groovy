
def call(repoName='ci-script',branch='sshTest'){
    checkout changelog: false, poll: false, scm: [
            $class                           : 'GitSCM',
            branches                         : [[name: "*/$branch"]],
            doGenerateSubmoduleConfigurations: false,
            extensions                       : [
                    [$class: 'CloneOption', depth: 0, honorRefspec: true, noTags: true, reference: '', shallow: true],
                    [$class: 'CleanBeforeCheckout'],
            ],
            userRemoteConfigs                : [
                    [credentialsId: '46afdff1-cdd3-4098-b8af-d904b4d298aa',
                     url          : "ssh://git@bitbucket.lombardrisk.com:7999/cprod/${repoName}.git"]
            ]
    ]
}