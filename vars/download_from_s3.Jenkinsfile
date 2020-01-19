@Library('pipeline-libs') 
import static com.lombardrisk.pipeline.Credentials.AWS

pipeline {
    agent { label 'PRODUCT-CI-SHA-LOCAL1' }
    options {
        timeout(time: 60, unit: 'MINUTES') 
    }
    
    stages {
        stage('checkout'){
			steps{
				echo "start job B ${JOB_URL}"
				echo "branch number: ${env.BUILD_NUMBER}"
				echo "${S3BUCKET}"
				echo "${S3DOWNPATH}"
				echo "${DOWNLOADFILENAMES}"
				sh 'pwd'
			}
		}
		stage('download package'){
			steps{
				echo "download all package"
                downloadProductPackage(S3BUCKET,S3DOWNPATH,DOWNLOADFILENAMES)
			}
		}
		
    }

}



void downloadProductPackage(s3bucket,s3repo,packageNames){

    String[] packageNameArr=packageNames.split(':')
    for(String packageName in packageNameArr){
        println packageName
        execute('s3 cp s3://'+s3bucket+'/'+s3repo+packageName+' /home/test/repository/'+s3repo+ packageName+' --no-progress ') //ssl error  --no-verify-ssl
    }
    
}


private def execute(String cmd) {
    withCredentials([usernamePassword(
            credentialsId: AWS,
            usernameVariable: 'AWS_ACCESS_KEY_ID',
            passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {

        String localBin = "${env.HOME}/.local/bin"

        withEnv(["PATH+LOCAL_BIN=$localBin"]) {
            sh "aws $cmd"
        }
    }
}

