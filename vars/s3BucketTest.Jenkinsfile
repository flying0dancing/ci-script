//noinspection GroovyUnusedAssignment
@Library('pipeline-libs') _

stage('setup') {
    node {
        loadLocalSteps()

        common.setupEnvVars()
    }
}

private void loadLocalSteps() {
    library identifier: 'ignis@master',
            retriever: modernSCM([
                    $class       : 'GitSCMSource',
                    remote       : "ssh://git@bitbucket.lombardrisk.com:7999/cs/ignis.git",
                    credentialsId: '4775e132-d845-4896-971d-f2a210ccdb02',
                    id           : 'e533458a-0409-4028-8de1-6adb3d17dd1f',
                    traits       : [
                            [$class: 'BranchDiscoveryTrait'],
                            [$class: 'CloneOptionTrait', extension: [depth: 0, noTags: true, shallow: true]]
                    ]
            ])
}

IGNIS_POM = 'pom.xml'

MVN = maven.initialiseMvn 'mvn-3'
BUILD_INFO = maven.newBuildInfo()
BUILD_VERSION = '1.0.0-SNAPSHOT'

IGNIS_PHOENIX_CLIENT_ROOT_DIR = 'ignis-phoenix-client'
IGNIS_PHOENIX_CLIENT_TARGET_DIR = "${IGNIS_PHOENIX_CLIENT_ROOT_DIR}/target"
IGNIS_PHOENIX_CLIENT_JAR = 'ignis-phoenix-4.13.0-HBase-1.3-client.jar'
IGNIS_PHOENIX_CLIENT_JAR_PATH = "ignis-spark-libs/${BUILD_VERSION}/${IGNIS_PHOENIX_CLIENT_JAR}"

pipeline {
    agent { label 'fcr-build' }

    environment { JAVA_HOME = tool 'OracleJDK8' }

    stages {
        stage('Build') {
            steps {
                buildPhoenixClient()
            }
        }
        stage('Temp Bucket') {
            steps {
                testTempBucketUpload()
                testTempBucketDownload()
                testTempBucketDelete()
            }
        }
        stage('Deploy Bucket') {
            steps {
                testDeployBucketUpload()
                testDeployBucketDownload()
            }
        }
        stage('Installers Bucket') {
            steps {
                testInstallersBucketUpload()
                testInstallersBucketDelete()
            }
        }
        stage('File Transfer Bucket') {
            steps {
                testFileTransferBucketUpload()
                testFileTransferBucketDownload()
                testFileTransferBucketDelete()
            }
        }
    }

    post {
        always {
            cleanUpIgnisArtifacts()
            cleanTarget()
        }
    }
}

private void buildPhoenixClient(buildInfo = BUILD_INFO) {
    MVN.run goals: "clean install -pl ${IGNIS_PHOENIX_CLIENT_ROOT_DIR}".toString(),
            pom: IGNIS_POM,
            buildInfo: buildInfo
}

private void testTempBucketUpload() {
    s3Temp.upload("$IGNIS_PHOENIX_CLIENT_TARGET_DIR/$IGNIS_PHOENIX_CLIENT_JAR", IGNIS_PHOENIX_CLIENT_JAR_PATH)
}

private void testTempBucketDownload() {
    dir(IGNIS_PHOENIX_CLIENT_TARGET_DIR) {
        s3Temp.download(IGNIS_PHOENIX_CLIENT_JAR_PATH)
    }
}

private void testTempBucketDelete() {
    s3Temp.delete(IGNIS_PHOENIX_CLIENT_JAR_PATH)
}

private void testDeployBucketUpload() {
    s3Deploy.upload("${IGNIS_PHOENIX_CLIENT_TARGET_DIR}/${IGNIS_PHOENIX_CLIENT_JAR}", IGNIS_PHOENIX_CLIENT_JAR_PATH)
}

private void testDeployBucketDownload() {
    dir(IGNIS_PHOENIX_CLIENT_TARGET_DIR) {
        s3Deploy.download(IGNIS_PHOENIX_CLIENT_JAR_PATH)
    }
}

private void testInstallersBucketUpload() {
    s3Installers.upload(
            localFile: "${IGNIS_PHOENIX_CLIENT_TARGET_DIR}/${IGNIS_PHOENIX_CLIENT_JAR}",
            remoteFile: "${BUILD_VERSION}/${IGNIS_PHOENIX_CLIENT_JAR}")
}

private void testInstallersBucketDelete() {
    s3Installers.cleanup(BUILD_VERSION)
}

private void testFileTransferBucketUpload() {
    s3Cli.put(
            bucket: 'ci-dev-arsuite-common-s3filetransferbucket-10aa38pm8qyy',
            localPath: 'ignis-functional-test/src/main/resources/datasets/',
            remotePath: 'testS3',
            encrypt: true,
            recursive: true)
}

private void testFileTransferBucketDownload() {
    s3Cli.get(
            bucket: 'ci-dev-arsuite-common-s3filetransferbucket-10aa38pm8qyy',
            localPath: IGNIS_PHOENIX_CLIENT_TARGET_DIR,
            remotePath: 'testS3/',
            recursive: true)
}

private void testFileTransferBucketDelete() {
    s3Cli.delete(
            bucket: 'ci-dev-arsuite-common-s3filetransferbucket-10aa38pm8qyy',
            remotePath: 'testS3',
            recursive: true)
}

private void cleanUpIgnisArtifacts() {
    cleanUpLocalArtifacts groupId: 'com.lombardrisk'
}

private void cleanTarget() {
    MVN.run goals: '-q clean', pom: IGNIS_POM
}