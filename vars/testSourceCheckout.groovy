
def call(projectFolder,qaRepoName,branch='master'){
    checkout([$class: 'GitSCM',
              branches: [[name: "*/${branch}"]],
              doGenerateSubmoduleConfigurations: false,
              extensions: [
                      [$class: 'CleanBeforeCheckout'],
                      [$class: 'RelativeTargetDirectory', relativeTargetDir: "${projectFolder}"]
              ],
              submoduleCfg: [],
              userRemoteConfigs: [
                      [credentialsId: '46afdff1-cdd3-4098-b8af-d904b4d298aa',
                       url: "ssh://git@bitbucket.lombardrisk.com:7999/cprod/${qaRepoName}.git"]
              ]
    ])
}