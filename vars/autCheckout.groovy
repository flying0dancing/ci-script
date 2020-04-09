
def call(targetDir,repoName='trump',branch='branch_v1.2.1'){
    def proj='cprod'
    if(repoName.equalsIgnoreCase('trump') || repoName.equalsIgnoreCase('public')){
        proj='aut'
    }
    if(!targetDir){
        targetDir=''
    }
    checkout changelog: false, poll: false, scm: [
            $class: 'GitSCM',
            branches: [[name: "*/${branch}"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                    [$class: 'RelativeTargetDirectory', relativeTargetDir: "${targetDir}"],
                    [$class: 'IgnoreNotifyCommit'],
                    [$class: 'CleanBeforeCheckout'],
                    [$class: 'CloneOption', depth: 0, honorRefspec: true, noTags: true, reference: '', shallow: true]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [
                    [credentialsId: '4775e132-d845-4896-971d-f2a210ccdb02',
                     url: "ssh://git@bitbucket.lombardrisk.com:7999/${proj}/${repoName}.git"]
            ]
    ]
}

/*
trumpCheckout('trump','trump','branch_v1.2.1')
trumpCheckout('trump/public','public','arproduct')
trumpCheckout('ci-script','ci-script','branch_v1.2.1')
*/