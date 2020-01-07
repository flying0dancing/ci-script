void call(String branch = 'master') {

    checkout changelog: false, poll: false, scm: [
            $class                           : 'GitSCM',
            branches                         : [[name: "*/$branch"]],
            doGenerateSubmoduleConfigurations: false,
            extensions                       : [
                    [$class: 'CloneOption', depth: 0, honorRefspec: true, noTags: true, reference: '', shallow: true],
                    [$class: 'CleanBeforeCheckout'],
            ],
            userRemoteConfigs                : [
                    [credentialsId: '4775e132-d845-4896-971d-f2a210ccdb02',
                     url          : 'ssh://git@bitbucket.lombardrisk.com:7999/cs/ignis.git']
            ]
    ]
}