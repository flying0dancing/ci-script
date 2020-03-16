#!groovy

ARProject_Prefix=getprojectFolder(env.JOB_NAME)

pipeline{
    //agent { label 'PRODUCT-CI-SHA-LOCAL-WIN' }
    //agent { label 'PRODUCT-CI-SHA-LOCAL1' }
    agent { label 'master' }
    stages{
        stage('checkout') {
            steps {
                script {
                    sourceCheckout(ARProject_Prefix,'master')
                }
            }
        }
        stage("trigger others"){
            steps{
                script{
                    echo "trigger other job"
                    build ARProject_Prefix+'_package'
                }
            }
        }

    }
    post {
        always {
            echo "this is always run"
        }
    }
}
String getprojectFolder(jobName){
	return jobName[0..jobName.indexOf("_")-1]
}

def sourceCheckout(repoName,branch='master'){
    checkout scm: [
            $class: 'GitSCM',
            branches: [[name: "*/${branch}"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                    [$class: 'CleanBeforeCheckout'],
                    [$class: 'CloneOption', depth: 0, honorRefspec: true, noTags: true, reference: '', shallow: true]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [
                    [credentialsId: '4775e132-d845-4896-971d-f2a210ccdb02',
                     url: "ssh://git@bitbucket.lombardrisk.com:7999/cprod/${repoName}.git"]
            ]
    ]
}

