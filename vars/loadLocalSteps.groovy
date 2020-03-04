
def call(repoName='ci-script',branch='sshTest'){
    library identifier: "ci-script@$branch",
            retriever: modernSCM([
                    $class: 'GitSCMSource',
                    credentialsId: '46afdff1-cdd3-4098-b8af-d904b4d298aa',
                    id: 'a58b1061-f557-46f6-ba36-b53cfdb77d43',
                    remote: "ssh://git@bitbucket.lombardrisk.com:7999/cprod/${repoName}.git",
                    traits: [[$class: 'BranchDiscoveryTrait']]])
}