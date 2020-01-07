//noinspection GroovyUnusedAssignment
@Library('pipeline-libs') _

import static com.lombardrisk.pipeline.Credentials.AWS

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

BUILD_VERSION = "2.3.0-b${env.BUILD_NUMBER}"
IGNIS_POM = 'pom.xml'

MVN = maven.initialiseMvn 'mvn-3'
BUILD_INFO = maven.newBuildInfo()

IGNIS_SERVER_INSTALLER_DIR = 'ignis-server-installer/ignis-server-izpack-installer/target'
IGNIS_SERVER_ZIP = "fcr-engine-ignis-server-${BUILD_VERSION}.zip"
IGNIS_SERVER_S3_DEPLOY_ZIP_PATH = "ignis-server/${BUILD_VERSION}/${IGNIS_SERVER_ZIP}"

IGNIS_ERASER_INSTALLER_DIR = 'ignis-server-parent/ignis-server-eraser/target'
IGNIS_ERASER_ZIP = "fcr-engine-ignis-eraser-${BUILD_VERSION}.zip"
IGNIS_ERASER_S3_DEPLOY_ZIP_PATH = "ignis-eraser/${BUILD_VERSION}/${IGNIS_ERASER_ZIP}"

DESIGN_STUDIO_INSTALLER_DIR = 'ignis-design/ignis-design-server/target'
DESIGN_STUDIO_ZIP = "fcr-engine-design-studio-${BUILD_VERSION}.zip"
DESIGN_STUDIO_S3_DEPLOY_ZIP_PATH = "design-studio/${BUILD_VERSION}/${DESIGN_STUDIO_ZIP}"

IGNIS_PHOENIX_CLIENT_ROOT_DIR = 'ignis-phoenix-client'
IGNIS_PHOENIX_CLIENT_TARGET_DIR = "${IGNIS_PHOENIX_CLIENT_ROOT_DIR}/target"
IGNIS_PHOENIX_CLIENT_JAR = 'ignis-phoenix-4.13.0-HBase-1.3-client.jar'
IGNIS_PHOENIX_CLIENT_JAR_PATH = "ignis-spark-libs/${BUILD_VERSION}/${IGNIS_PHOENIX_CLIENT_JAR}"

plaformToolsChanged = false
PLATFORM_TOOLS_INSTALLER_ROOT_DIR = 'ignis-platform-tools-installer/ignis-platform-tools-izpack-installer'
PLATFORM_TOOLS_INSTALLER_DIR = "${PLATFORM_TOOLS_INSTALLER_ROOT_DIR}/target"
PLATFORM_TOOLS_ZIP = "fcr-engine-platform-tools-${BUILD_VERSION}.zip"
PLATFORM_TOOLS_S3_DEPLOY_ZIP_PATH = "platform-tools/${BUILD_VERSION}/${PLATFORM_TOOLS_ZIP}"

IGNIS_SERVER_INSTALLER_ROOT_DIR = 'ignis-server-installer/ignis-server-izpack-installer'
DESIGN_STUDIO_INSTALLER_ROOT_DIR = 'ignis-design/ignis-design-server'
IGNIS_ERASER_INSTALLER_ROOT_DIR = 'ignis-server-parent/ignis-server-eraser'

INSTALLER_PROPERTIES_STASH = 'installer-properties'

FUNCTIONAL_TEST_DATASETS_SOURCE_PATH = 'ignis-functional-test/src/main/resources/datasets/'

// Cloudformation Properties
CLOUDFORMATION_TEMPLATES_DIR = 'ignis-cloudformation-templates'
CLIENT_NAME = 'LRM'
ENVIRONMENT_TYPE = 'DEV'
PRIVATE_SUBNET_IDS = 'subnet-b50e3bd1, subnet-098ba57f, subnet-bab6c0e2'
EMR_SUBNET_ID = 'subnet-b50e3bd1'
EC2_KEY_NAME = 'LRM-SRVCAT-KEYPAIR'
BUILD_AGENT_SECURITY_GROUP = 'sg-a800ffd1'
RDS_MASTER_USER = 'lrmmdb'
RDS_MASTER_PASSWORD = 'xgWV8WBzYxwslhT'
RDS_OWNER_USER = 'IGNIS$OWNER'
RDS_OWNER_PASSWORD = 'password'
IGNIS_ADMIN_PASSWORD = 'password'
AR_APP_SECURITY_GROUP = BUILD_AGENT_SECURITY_GROUP//we don't care about AR for this pipeline
ASGAMI='ami-005835c4fe66bd5fd'

COMMON_DEV_STACK = [
        name    : "${CLIENT_NAME}-${ENVIRONMENT_TYPE}-ARSUITE-COMMON",
        template: "${CLOUDFORMATION_TEMPLATES_DIR}/Common/AR-Suite_common-cfv1.yaml"
]

EMR_DEV_STACK = [
        name    : "${CLIENT_NAME}-${ENVIRONMENT_TYPE}-FCRENGINE-EMR",
        template: "${CLOUDFORMATION_TEMPLATES_DIR}/FCR-Engine/FCR-EMR-Cluster-cfv1.yaml"
]

FCR_DEV_STACK = [
        name    : "${CLIENT_NAME}-${ENVIRONMENT_TYPE}-FCRENGINE-APPSERVER",
        template: "${CLOUDFORMATION_TEMPLATES_DIR}/FCR-Engine/FCR-Ignis-Server-cfv1.yaml"
]

def commonStackOutputs
def emrStackOutputs
def appServerStackOutputs
def s3DatasetsBucket

pipeline {
    agent { label 'fcr-build' }

    options { skipDefaultCheckout true }

    environment { JAVA_HOME = tool 'OracleJDK8' }

    stages {
        stage('Version') {
            steps {
                checkout scm

                generateVersion()

                buildAndUploadPhoenixClient()
            }
        }
        stage('Build and Create SaaS Environment') {
            parallel {
                stage('Build') {
                    steps {
                        runBuild()

                        uploadZipsToS3TempBucket()

                        stashInstallConfig()

                        runSonarAnalysis()
                    }
                    post {
                        always {
                            junit '**/target/*-reports/*.xml'
                        }
                    }
                }
                stage('Create SaaS Environment') {
                    when {
                        expression {
                            false
                        }
                    }
                    steps {
                        dir(CLOUDFORMATION_TEMPLATES_DIR) {
                            git(
                                    url: 'ssh://git@bitbucket.lombardrisk.com:7999/acf/ar-suite.git',
                                    branch: 'master',
                                    credentialsId: 'ab6cac62-d0df-4f17-bfdb-e82c72d62d13',
                                    changelog: false,
                                    poll: false
                            )
                        }

                        script {
                            commonStackOutputs = describeCommonStack()
                            s3DatasetsBucket = commonStackOutputs.S3FileTransferBucketName

                            emrStackOutputs = setupEMRCluster()
                        }
                    }
                }
            }
        }
        stage('Release Design Studio') {
            agent { label 'fcr-cluster-env' }

            environment { JAVA_HOME = tool 'OracleJDK8' }

            steps {
                releaseDesignStudioTo FcrEnvironment.CLUSTER
            }
        }
        stage('Release and Test') {
            parallel {
                stage('Standalone Env') {
                    agent { label 'fcr-standalone-env' }

                    environment { JAVA_HOME = tool 'OracleJDK8' }

                    steps {
                        deleteDir()

                        shallowCheckout()

                        releasePlatformToolsTo FcrEnvironment.STANDALONE
                        buildFunctionalTestDependencies()

                        releaseIgnisServerTo FcrEnvironment.STANDALONE

                        releaseIgnisEraserTo FcrEnvironment.STANDALONE

                        copyDatasets FcrEnvironment.STANDALONE

                        runFunctionalTestsFor env: FcrEnvironment.STANDALONE
                    }
                    post {
                        always {
                            cleanUpIgnisArtifacts()

                            junit 'ignis-functional-test/target/standalone-env-failsafe-reports/*.xml'
                        }
                    }
                }
                stage('Cluster Env') {
                    agent { label 'fcr-cluster-env' }

                    environment { JAVA_HOME = tool 'OracleJDK8' }

                    steps {
                        deleteDir()

                        shallowCheckout()

                        releasePlatformToolsTo FcrEnvironment.CLUSTER
                        buildFunctionalTestDependencies()

                        releaseIgnisServerTo FcrEnvironment.CLUSTER

                        releaseIgnisEraserTo FcrEnvironment.CLUSTER

                        copyDatasets FcrEnvironment.CLUSTER

                        runFunctionalTestsFor env: FcrEnvironment.CLUSTER
                    }
                    post {
                        always {
                            cleanUpIgnisArtifacts()

                            junit 'ignis-functional-test/target/cluster-env-failsafe-reports/*.xml'
                        }
                    }
                }
                stage('SaaS Env') {
                    when {
                        expression {
                            false
                        }
                    }
                    steps {
                        script {
                            appServerStackOutputs = setupAppServer(emrStackOutputs)

                            String loadBalancerAddress = commonStackOutputs.LoadBalancerDNS
                            String emrMasterIP = parsePrivateDnsName(emrStackOutputs.EMRClusterAWSDNS)

                            checkIgnisServerIsHealthy host: "${loadBalancerAddress}", port: 443

                            copyDatasetsToS3(s3DatasetsBucket)

                            runFunctionalTestsFor(
                                    env: FcrEnvironment.SASS,
                                    properties: "-Dignis-server.host=${loadBalancerAddress}" +
                                            " -Demr.master.host=${emrMasterIP}" +
                                            " -Drds.endpoint=${commonStackOutputs.DatabaseEndPoint}" +
                                            " -Drds.port=${commonStackOutputs.RDnonSSSLPortValue}"
                            )
                        }
                    }
                    post {
                        always {
                            destroySassEnv()
                            junit 'ignis-functional-test/target/sass-env-failsafe-reports/*.xml'
                        }
                    }
                }
            }
        }
        stage('Upload Candidate Release') {
            steps {
                deployArtifactsToS3()

                reportReleased(
                        IGNIS_SERVER_S3_DEPLOY_ZIP_PATH,
                        DESIGN_STUDIO_S3_DEPLOY_ZIP_PATH,
                        PLATFORM_TOOLS_S3_DEPLOY_ZIP_PATH,
                        IGNIS_PHOENIX_CLIENT_JAR_PATH)

                deployArtifactsToArtifactory()

                tagCommit()
            }
        }
    }
    post {
        changed {
            script {
                notifier.sendFixed()
            }
        }
        always {
            cleanUpIgnisArtifacts()
            cleanTarget()
            cleanS3InstallersBucket()
        }
    }
}

enum FcrEnvironment {
    STANDALONE([
            user            : 'ubuntu',
            systemProperties: 'standalone-env/environment.properties',
            cluster         : false,
            s3              : false,
            mvnTestProfile  : 'standalone-env-functional-tests'
    ]),
    CLUSTER([
            user            : 'ubuntu',
            systemProperties: 'cluster-env/environment.properties',
            cluster         : true,
            s3              : false,
            mvnTestProfile  : 'cluster-env-functional-tests'
    ]),
    SASS([
            user            : 'ec2-user',
            systemProperties: 'NOOP',
            cluster         : false,
            s3              : true,
            mvnTestProfile  : 'sass-env-functional-tests'
    ])
    final String home
    final String systemProperties
    final String stashName
    final boolean cluster
    final boolean s3
    final String mvnTestProfile

    FcrEnvironment(Map args) {
        this.home = "/home/${args.user}"
        this.systemProperties = "scripts/${args.systemProperties}"
        this.stashName = systemProperties.replace('/', '-')
        this.cluster = args.cluster
        this.s3 = args.s3
        this.mvnTestProfile = args.mvnTestProfile
    }
}

private void generateVersion(buildInfo = BUILD_INFO) {
    notifier.runAndNotifyOnFailure("Failed to version project with $BUILD_VERSION") {
        MVN.run goals: "-q versions:set versions:commit -DnewVersion=$BUILD_VERSION".toString(),
                pom: IGNIS_POM,
                buildInfo: buildInfo
    }
}

private void buildAndUploadPhoenixClient(buildInfo = BUILD_INFO) {
    MVN.run goals: "clean install -pl ${IGNIS_PHOENIX_CLIENT_ROOT_DIR}".toString(),
            pom: IGNIS_POM,
            buildInfo: buildInfo

    s3Installers.upload(
            localFile: "${IGNIS_PHOENIX_CLIENT_TARGET_DIR}/${IGNIS_PHOENIX_CLIENT_JAR}",
            remoteFile: "${BUILD_VERSION}/${IGNIS_PHOENIX_CLIENT_JAR}")
}

private void runBuild(buildInfo = BUILD_INFO) {
    notifier.runAndNotifyOnFailure("Failed to build project") {
        configFileProvider([configFile(fileId: 'default-maven-settings', variable: 'MAVEN_SETTINGS_XML')]) {
            MVN.run goals: """clean install
                                  -T1C
                                  -s ${MAVEN_SETTINGS_XML}
                                  -Pjenkins
                                  -pl !ignis-functional-test
                                  -pl !${IGNIS_PHOENIX_CLIENT_ROOT_DIR}  
                                  -pl !${DESIGN_STUDIO_INSTALLER_ROOT_DIR}
                                  -pl !${IGNIS_SERVER_INSTALLER_ROOT_DIR}
                                  -pl !${IGNIS_ERASER_INSTALLER_ROOT_DIR}
                                  -pl !${PLATFORM_TOOLS_INSTALLER_ROOT_DIR}""".toString(),
                    pom: IGNIS_POM,
                    buildInfo: buildInfo

            MVN.run goals: """clean install
                                  -T1C
                                  -s ${MAVEN_SETTINGS_XML}
                                  -P archive                                   
                                  -pl ${DESIGN_STUDIO_INSTALLER_ROOT_DIR}
                                  -pl ${IGNIS_SERVER_INSTALLER_ROOT_DIR}
                                  -pl ${IGNIS_ERASER_INSTALLER_ROOT_DIR}""".toString(),
                    pom: IGNIS_POM,
                    buildInfo: buildInfo
        }
    }

    runWhenFilesChanged(PLATFORM_TOOLS_INSTALLER_ROOT_DIR) {
        notifier.runAndNotifyOnFailure("Failed to build platform-tools installer") {

            MVN.run goals: """clean package
                                  -P archive
                                  -pl $PLATFORM_TOOLS_INSTALLER_ROOT_DIR -am""".toString(),
                    pom: IGNIS_POM,
                    buildInfo: buildInfo
            plaformToolsChanged = true
        }
    }
}

private void uploadZipsToS3TempBucket() {
    dir(IGNIS_SERVER_INSTALLER_DIR) {
        s3Temp.upload(IGNIS_SERVER_ZIP, IGNIS_SERVER_S3_DEPLOY_ZIP_PATH)
        s3Installers.upload(localFile: IGNIS_SERVER_ZIP, remoteFile: "$BUILD_VERSION/$IGNIS_SERVER_ZIP")
    }
    dir(IGNIS_ERASER_INSTALLER_DIR) {
        s3Temp.upload(IGNIS_ERASER_ZIP, IGNIS_ERASER_S3_DEPLOY_ZIP_PATH)
    }
    dir(DESIGN_STUDIO_INSTALLER_DIR) {
        s3Temp.upload(DESIGN_STUDIO_ZIP, DESIGN_STUDIO_S3_DEPLOY_ZIP_PATH)
    }
    dir(IGNIS_PHOENIX_CLIENT_TARGET_DIR) {
        s3Temp.upload(IGNIS_PHOENIX_CLIENT_JAR, IGNIS_PHOENIX_CLIENT_JAR_PATH)
    }
    if (plaformToolsChanged) {
        dir(PLATFORM_TOOLS_INSTALLER_DIR) {
            s3Temp.upload(PLATFORM_TOOLS_ZIP, PLATFORM_TOOLS_S3_DEPLOY_ZIP_PATH)
        }
    }
}

private void stashInstallConfig() {
    dir("scripts") {
        stash includes: '*.properties', name: INSTALLER_PROPERTIES_STASH
    }
    dir("scripts/standalone-env") {
        stash name: FcrEnvironment.STANDALONE.stashName
    }
    dir("scripts/cluster-env") {
        stash name: FcrEnvironment.CLUSTER.stashName
    }
}

private void releaseDesignStudioTo(FcrEnvironment fcrEnv) {
    unstash INSTALLER_PROPERTIES_STASH
    unstash fcrEnv.stashName

    notifier.runAndNotifyOnFailure("Failed to release Design Studio to cluster env") {
        s3Temp.download(DESIGN_STUDIO_S3_DEPLOY_ZIP_PATH)

        releaseDesignStudio candidateReleasePath: DESIGN_STUDIO_S3_DEPLOY_ZIP_PATH,
                homeDir: fcrEnv.home,
                skipS3DeployDownload: true
    }
}

private void releasePlatformToolsTo(FcrEnvironment fcrEnv) {
    if (plaformToolsChanged) {
        notifier.runAndNotifyOnFailure("Failed to release platform-tools on ${fcrEnv.name()}") {

            unstash INSTALLER_PROPERTIES_STASH
            unstash fcrEnv.stashName

            s3Temp.download(PLATFORM_TOOLS_S3_DEPLOY_ZIP_PATH)

            releasePlatformTools candidateReleasePath: PLATFORM_TOOLS_S3_DEPLOY_ZIP_PATH,
                    skipS3DeployDownload: true,
                    cluster: fcrEnv == FcrEnvironment.CLUSTER
        }
    }
}

private void releaseIgnisServerTo(FcrEnvironment fcrEnv) {
    notifier.runAndNotifyOnFailure("Failed to release ignis-server on ${fcrEnv.name()}") {

        s3Temp.download(IGNIS_SERVER_S3_DEPLOY_ZIP_PATH)

        releaseIgnisServer candidateReleasePath: IGNIS_SERVER_S3_DEPLOY_ZIP_PATH,
                skipS3DeployDownload: true
    }

    echo "JenkinsFile: releaseIgnisServerTo ${fcrEnv} complete"
}

private void releaseIgnisEraserTo(FcrEnvironment fcrEnv) {
    s3Temp.download(IGNIS_ERASER_S3_DEPLOY_ZIP_PATH)
    releaseIgnisEraser candidateReleasePath: IGNIS_ERASER_S3_DEPLOY_ZIP_PATH,
            homeDir: fcrEnv.home,
            skipS3DeployDownload: true
}

private void buildFunctionalTestDependencies() {
    echo 'Building functional test dependencies'

    notifier.runAndNotifyOnFailure("Failed to build functional test dependencies") {
        MVN.run goals: "-q versions:set versions:commit -DnewVersion=$BUILD_VERSION".toString(),
                pom: IGNIS_POM

        MVN.run goals: "-q clean install -PskipTests -pl ignis-functional-test -am".toString(),
                pom: IGNIS_POM
    }
}

private void runFunctionalTestsFor(Map args) {
    echo 'Running Functional Tests'

    notifier.runAndNotifyOnFailure("Failed to run functional tests") {
        MVN.run goals: "clean verify -pl ignis-functional-test -P ${args.env.mvnTestProfile} ${args.properties ?: ''}".toString(),
                pom: IGNIS_POM
    }
}

private void copyDatasets(FcrEnvironment fcrEnv) {
    echo "Copying datasets from ${FUNCTIONAL_TEST_DATASETS_SOURCE_PATH}/ to ${fcrEnv.home}/ignis/data/datasets"

    sh "cp -v ${FUNCTIONAL_TEST_DATASETS_SOURCE_PATH}/*.csv ${fcrEnv.home}/ignis/data/datasets"
}

private void copyDatasetsToS3(fileTransferBucket) {
    echo "Uploading files to S3 bucket '$fileTransferBucket'"

    //TODO: replace this with AWS pipeline step after Jenkins and plugins have been upgraded
    s3Cli.put(
            bucket: fileTransferBucket,
            localPath: FUNCTIONAL_TEST_DATASETS_SOURCE_PATH,
            remotePath: 'upload',
            encrypt: true,
            recursive: true)
}

private void runSonarAnalysis() {
    notifier.runAndNotifyOnFailure('Failed Sonar analysis') {
        MVN.run goals: 'sonar:sonar -pl !ignis-functional-test',
                pom: IGNIS_POM,
                buildInfo: BUILD_INFO
    }
}

private void deployArtifactsToS3() {
    notifier.runAndNotifyOnFailure("Failed to publish build info to S3 deploy bucket") {
        dir(IGNIS_SERVER_INSTALLER_DIR) {
            s3Deploy.upload(IGNIS_SERVER_ZIP, IGNIS_SERVER_S3_DEPLOY_ZIP_PATH)
        }
        dir(IGNIS_ERASER_INSTALLER_DIR) {
            s3Deploy.upload(IGNIS_ERASER_ZIP, IGNIS_ERASER_S3_DEPLOY_ZIP_PATH)
        }
        dir(DESIGN_STUDIO_INSTALLER_DIR) {
            s3Deploy.upload(DESIGN_STUDIO_ZIP, DESIGN_STUDIO_S3_DEPLOY_ZIP_PATH)
        }
        dir(IGNIS_PHOENIX_CLIENT_TARGET_DIR) {
            s3Deploy.upload(IGNIS_PHOENIX_CLIENT_JAR, IGNIS_PHOENIX_CLIENT_JAR_PATH)
        }
        if (plaformToolsChanged) {
            dir(PLATFORM_TOOLS_INSTALLER_DIR) {
                s3Deploy.upload(PLATFORM_TOOLS_ZIP, PLATFORM_TOOLS_S3_DEPLOY_ZIP_PATH)
            }
        }
    }
}

private void deployArtifactsToArtifactory() {
    notifier.runAndNotifyOnFailure("Failed to publish build info to Artifactory") {
        MVN.deployer.artifactDeploymentPatterns
                .addExclude("*.zip")
                .addExclude("ignis-phoenix-*-client.jar")
                .addExclude("ignis-spark-staging-*.jar")
                .addExclude("ignis-spark-validation-*.jar")
                .addExclude("ignis-design-*.jar")
                .addExclude("ignis-server-*.jar")
                .addExclude("ignis-platform-*.jar")
                .addExclude("ignis-functional-test-*.jar")

        MVN.deployer.deployArtifacts BUILD_INFO
        maven.artifactoryServer().publishBuildInfo BUILD_INFO
    }
}

private void reportReleased(ignisServerZip, designStudioZip, platformToolsZip, ignisPhoenixClientJar) {
    if (plaformToolsChanged) {
        reportReleases(ignisServerZip, designStudioZip, platformToolsZip, ignisPhoenixClientJar)
    } else {
        reportReleases(ignisServerZip, designStudioZip, ignisPhoenixClientJar)
    }
}

private void tagCommit() {
    notifier.runAndNotifyOnFailure('Failed to tag git commit') {
        bitbucketTag projectPath: 'projects/CS/repos/ignis',
                tag: BUILD_VERSION
    }
}

private void cleanUpIgnisArtifacts() {
    cleanUpLocalArtifacts groupId: 'com.lombardrisk'
}

private void cleanTarget() {
    MVN.run goals: '-q clean', pom: IGNIS_POM
}

private void cleanS3InstallersBucket() {
    s3Installers.cleanup(BUILD_VERSION)
}

private Map describeCommonStack() {
    echo "Describing Common stack"

    return cloudFormation.describe(stack: COMMON_DEV_STACK.name)
}

private Map setupEMRCluster() {
    echo 'Deploying EMR stack'

    return cloudFormation.setup(
            stack: EMR_DEV_STACK.name,
            file: EMR_DEV_STACK.template,
            params: [
                    "ClientName=$CLIENT_NAME",
                    "EnvironmentType=$ENVIRONMENT_TYPE",
                    "ResponsibleOwnerEmail=msibson@vermeg.com",
                    "OwnerEmail=matthew.sibson@lombardrisk.com",
                    "EMRSubnetId=$EMR_SUBNET_ID",
                    "SecurityGroupAREMRAccess=$AR_APP_SECURITY_GROUP",
                    "EMRRelease=emr-5.12.2",
                    "FCREngineVersion=$BUILD_VERSION",
                    "KeyName=$EC2_KEY_NAME",
                    "ARSuiteCommonStackName=${COMMON_DEV_STACK.name}",
                    "JenkinsSourceSecurityGroupId=$BUILD_AGENT_SECURITY_GROUP"
            ]
    )
}

private Map setupAppServer(emrStackOutputs) {
    echo 'Deploying EC2 app server stack'

    String emrMasterIP = parsePrivateDnsName(emrStackOutputs.EMRClusterAWSDNS)

    return cloudFormation.setup(
            stack: FCR_DEV_STACK.name,
            file: FCR_DEV_STACK.template,
            params: [
                    "ClientName=$CLIENT_NAME",
                    "EnvironmentType=$ENVIRONMENT_TYPE",
                    "ResponsibleOwnerEmail=msibson@vermeg.com",
                    "OwnerEmail=matthew.sibson@lombardrisk.com",
                    "PrivateSubnetIds=$PRIVATE_SUBNET_IDS",
                    "KeyName=$EC2_KEY_NAME",
                    "ARSuiteCommonStackName=${COMMON_DEV_STACK.name}",
                    "ExistingARSecurityGroupAppServer=$AR_APP_SECURITY_GROUP",
                    "DatabaseMasterPassword=$RDS_MASTER_PASSWORD",
                    "FCRDatabaseUsername=$RDS_OWNER_USER",
                    "FCRDatabasePassword=$RDS_OWNER_PASSWORD",
                    "ApplicationVersion=$BUILD_VERSION",
                    "IgnisAdminPassword=$IGNIS_ADMIN_PASSWORD",
                    "JenkinsSourceSecurityGroupId=$BUILD_AGENT_SECURITY_GROUP",
                    "EMRMasterNodeIP=$emrMasterIP",
                    "AppEMRAccessSecurityGroup=${emrStackOutputs.FCRSourceSecurityGroupId}",
                    "ASGAMI=${ASGAMI}",
            ]
    )
}

/**
 * Hack to use the private IP of the EC2 instance in LRM-SRVCAT-VPC vpc since private DNS doesn't work
 * (e.g. ip-10-96-4-216.eu-west-1.compute.internal)
 */
private String parsePrivateDnsName(dnsName) {
    def ipOctets = (dnsName =~ /ip-([0-9]{1,3})-([0-9]{1,3})-([0-9]{1,3})-([0-9]{1,3}).*/)
    return "${ipOctets[0][1]}.${ipOctets[0][2]}.${ipOctets[0][3]}.${ipOctets[0][4]}"
}

private void destroySassEnv() {
    cloudFormation.destroy stack: FCR_DEV_STACK.name
    cloudFormation.destroy stack: EMR_DEV_STACK.name
}
