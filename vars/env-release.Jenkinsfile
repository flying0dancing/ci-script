@Library('pipeline-libs') _

stage('Setup') {
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

ENVS = [
        'fcr-qa-cluster'        : [
                homeDir  : '/home/ubuntu',
                configDir: 'scripts/qa-cluster-env',
                cluster  : true,
                ar       : [
                        host: 'i-08bf7fed60e664856.internal.aws.lombardrisk.com',
                        port: 9080
                ],
                skipS3DeployDownload: false
        ],
        'fcr-us-uat-standalone' : [
                homeDir  : '/home/ec2-user',
                configDir: 'scripts/us-uat-env',
                cluster  : false,
                ar       : [
                        host: 'localhost',
                        port: 9080
                ],
                skipS3DeployDownload: false
        ],
        'fcr-sha-uat-standalone' : [
                homeDir  : '/home/test',
                configDir: 'scripts/sha-env',
                cluster  : false,
                skipS3DeployDownload: false
        ],
        'fcr-sha-qa-1-standalone' : [
                homeDir  : '/home/test',
                configDir: 'scripts/sha-qa1-env',
                cluster  : false,
                skipS3DeployDownload: false
        ],
        'fcr-sha-qa-2-standalone' : [
                homeDir  : '/home/test',
                configDir: 'scripts/sha-qa2-env',
                cluster  : false,
                skipS3DeployDownload: false
        ],
        'fcr-ldn-uat-standalone': [
                homeDir  : '/home/lr-user',
                configDir: 'scripts/ldn-uat-env',
                cluster  : false,
                ar       : [
                        host: 'localhost',
                        port: 9080
                ],
                skipS3DeployDownload: true,
                host: 'lr-user@ldn-fcr-uat.london.lombardrisk.com',
                credentials: 'lr-ldn-uat'
        ],
        'fcr-tun-uat-standalone': [
                homeDir  : '/home/server',
                configDir: 'scripts/tun-uat-env',
                cluster  : false,
                ar       : [
                        host: 'localhost',
                        port: 9080
                ],
                skipS3DeployDownload: true,
                host: 'server@10.2.140.98',
                credentials: 'fcr-tun-uat-standalone'
        ],
        'fcr-standalone-env'    : [
                homeDir  : '/home/ubuntu',
                configDir: 'scripts/standalone-env',
                cluster  : false,
                skipS3DeployDownload: false
        ],
        'fcr-cluster-env'       : [
                homeDir  : '/home/ubuntu',
                configDir: 'scripts/cluster-env',
                cluster  : true,
                skipS3DeployDownload: false
        ],
]
ENV_LABELS = """
fcr-qa-cluster
fcr-us-uat-standalone
fcr-sha-uat-standalone
fcr-sha-qa-1-standalone
fcr-sha-qa-2-standalone
fcr-ldn-uat-standalone
fcr-tun-uat-standalone
fcr-standalone-env
fcr-cluster-env"""

SELECTED_ENV = [:]
if (params.ENV_LABEL) {
    SELECTED_ENV = ENVS[params.ENV_LABEL]
}

def platformToolsCandidateRelease
def ignisServerCandidateRelease
def ignisEraserCandidateRelease
def designStudioCandidateRelease
def agileReporterCandidateRelease

def agentWorkspace

pipeline {
    agent { label 'fcr-build' }
    options { skipDefaultCheckout true }

    parameters {
        booleanParam name: 'INTERACTIVE',
                description: 'Request user input',
                defaultValue: true

        choice name: 'ENV_LABEL',
                choices: ENV_LABELS,
                description: 'Environment label to use for deploying releases. Required'

        string name: 'DESIGN_STUDIO_VERSION',
                description: 'Design Studio candidate release version. When empty, a dropdown of all versions will be displayed.'

        string name: 'PLATFORM_TOOLS_VERSION',
                description: 'Platform tools candidate release version. When empty, a dropdown of all versions will be displayed.'

        booleanParam name: 'HADOOP_UPGRADE',
                description: 'Whether to upgrade platform-tools Hadoop cluster. Set this to true when Hadoop version has changed between platform-tools versions.',
                defaultValue: false

        string name: 'IGNIS_SERVER_VERSION',
                description: 'Ignis server candidate release version. When empty, a dropdown of all versions will be displayed.'

        booleanParam name: 'INSTALL_IGNIS_ERASER',
                description: 'Install Ignis Eraser application (minimum version 2.3.0-b1004)',
                defaultValue: true

        booleanParam name: 'INSTALL_AR',
                description: 'Install AgileREPORTER flag',
                defaultValue: false
        string name: 'AR_VERSION',
                description: 'AgileREPORTER candidate release version. When empty, a dropdown of all versions will be displayed.'
    }
    stages {
        stage('Setup Candidate Release Versions') {
            steps {
                script {
                    checkEnvIsSelected(params.ENV_LABEL, SELECTED_ENV)

                    designStudioCandidateRelease = releaseFinder.designStudio(params.DESIGN_STUDIO_VERSION, params.INTERACTIVE)

                    platformToolsCandidateRelease = releaseFinder.platformTools(params.PLATFORM_TOOLS_VERSION, params.INTERACTIVE)
                    ignisServerCandidateRelease = releaseFinder.ignisServer(params.IGNIS_SERVER_VERSION, params.INTERACTIVE)
                    ignisEraserCandidateRelease = findIgnisEraserRelease(
                            params.INSTALL_IGNIS_ERASER, params.IGNIS_SERVER_VERSION, params.INTERACTIVE)

                    agileReporterCandidateRelease = findAgileReporterRelease(params.INSTALL_AR, params.AR_VERSION)

                    if (params.INTERACTIVE == true) {
                        showConfirmation(
                                params.ENV_LABEL,
                                designStudioCandidateRelease,
                                platformToolsCandidateRelease,
                                ignisServerCandidateRelease,
                                params.INSTALL_AR,
                                agileReporterCandidateRelease)
                    }
                }
            }
        }
        stage('Setup Workspace') {
            agent { label params.ENV_LABEL }

            steps {
                script {
                    deleteDir()

                    setupConfigFiles(SELECTED_ENV)

                    agentWorkspace = env.WORKSPACE
                }
            }
        }
        stage('Copy Installers') {
            // This stage is only executed as a workaround for environments that have been closed off from AWS
            when {
                expression {
                    return SELECTED_ENV.skipS3DeployDownload
                }
            }
            steps {
                copyToEnvironment(SELECTED_ENV, designStudioCandidateRelease, agentWorkspace)
                copyToEnvironment(SELECTED_ENV, platformToolsCandidateRelease, agentWorkspace)
                copyToEnvironment(SELECTED_ENV, ignisServerCandidateRelease, agentWorkspace)
                copyToEnvironment(SELECTED_ENV, ignisEraserCandidateRelease, agentWorkspace)

                copyAgileReporterToEnvironment(SELECTED_ENV, agileReporterCandidateRelease, agentWorkspace)
            }
        }
        stage('Release') {
            agent { label params.ENV_LABEL }

            steps {
                releaseFcrEngineDesignStudio(SELECTED_ENV, designStudioCandidateRelease)

                releaseFcrEnginePlatformTools(SELECTED_ENV, platformToolsCandidateRelease)
                releaseFcrEngineIgnisServer(SELECTED_ENV, ignisServerCandidateRelease)
                releaseFcrEngineIgnisEraser(SELECTED_ENV, ignisEraserCandidateRelease)

                releaseAR(SELECTED_ENV, agileReporterCandidateRelease)

                reportReleases(
                        designStudioCandidateRelease,
                        platformToolsCandidateRelease,
                        ignisServerCandidateRelease,
                        agileReporterCandidateRelease)
            }
        }
    }
    post {
        changed {
            script {
                notifier.sendFixed()
            }
        }
    }
}

private void checkEnvIsSelected(String envLabel, Map selectedEnv) {
    if (!selectedEnv) {
        error "Parameter 'ENV_LABEL' must not be blank"
    }
    echo "Release to environment ${envLabel}:${selectedEnv}"
}

private String findIgnisEraserRelease(boolean installEraser, String ignisVersion, boolean interactive) {
    def eraserRelease = ''
    if (installEraser) {
        eraserRelease = releaseFinder.ignisEraser(ignisVersion, interactive)
    } else {
        echo 'Ignis Eraser release was skipped'
    }
    return eraserRelease;
}

private String findAgileReporterRelease(boolean installAr, String arVersion) {
    def arRelease = ''
    if (installAr) {
        arRelease = releaseFinder.agileReporter(arVersion)
    } else {
        echo 'AgileREPORTER release was skipped'
    }
    return arRelease;
}

private void showConfirmation(
        String envLabel,
        String designRelease,
        String platformRelease,
        String serverRelease,
        boolean installAr,
        String arRelease) {

    timeout(time: 2, unit: 'MINUTES') {
        input([
                message: """
Continue release using the following settings?
    
    ENVIRONMENT   : ${envLabel}

    design-studio : ${designRelease}
    platform-tools: ${platformRelease}
    ignis-server  : ${serverRelease}

    agile-reporter: ${installAr ? arRelease : 'skipped'}
""",
                ok     : 'Release',
        ])
    }
}

private void setupConfigFiles(Map selectedEnv) {
    shallowCheckout()

    sh "cp scripts/*.properties ${selectedEnv.configDir}"
}

private void releaseFcrEngineDesignStudio(Map selectedEnv, String candidateRelease) {
    notifier.runAndNotifyOnFailure("Failed to release Design Studio to [${params.ENV_LABEL}] env") {

        dir(selectedEnv.configDir) {
            releaseDesignStudio homeDir: selectedEnv.homeDir,
                    candidateReleasePath: candidateRelease,
                    skipS3DeployDownload: selectedEnv.skipS3DeployDownload
        }
    }
}

private void releaseFcrEnginePlatformTools(Map selectedEnv, String candidateRelease) {
    notifier.runAndNotifyOnFailure("Failed to release Platform Tools to [${params.ENV_LABEL}] env") {

        dir(selectedEnv.configDir) {
            releasePlatformTools candidateReleasePath: candidateRelease,
                    cluster: selectedEnv.cluster,
                    upgradePlatformTools: params.HADOOP_UPGRADE,
                    skipS3DeployDownload: selectedEnv.skipS3DeployDownload
        }
    }
}

private void releaseFcrEngineIgnisServer(Map selectedEnv, String candidateRelease) {
    notifier.runAndNotifyOnFailure("Failed to release Ignis Server to [${params.ENV_LABEL}] env") {

        dir(selectedEnv.configDir) {
            releaseIgnisServer candidateReleasePath: candidateRelease,
                    skipS3DeployDownload: selectedEnv.skipS3DeployDownload
        }
    }
}

private void releaseFcrEngineIgnisEraser(Map selectedEnv, String candidateRelease) {
    if (candidateRelease) {
        notifier.runAndNotifyOnFailure("Failed to release Ignis Eraser to [${params.ENV_LABEL}] env") {

            dir(selectedEnv.configDir) {
                releaseIgnisEraser homeDir: selectedEnv.homeDir,
                        candidateReleasePath: candidateRelease,
                        skipS3DeployDownload: selectedEnv.skipS3DeployDownload
            }
        }
    }
}

private void releaseAR(Map selectedEnv, String candidateRelease) {
    if (candidateRelease) {
        notifier.runAndNotifyOnFailure("Failed to release AgileREPORTER to [${params.ENV_LABEL}] env") {
            dir(selectedEnv.configDir) {
                releaseAgileReporter([
                        installerJar: candidateRelease,
                        homeDir     : selectedEnv.homeDir,
                        arHost      : selectedEnv.ar.host,
                        arPort      : selectedEnv.ar.port,
                        skipS3DeployDownload: selectedEnv.skipS3DeployDownload
                ])
            }
        }
    }
}

private void copyToEnvironment(Map selectedEnv, String candidateReleasePath, String agentWorkspace) {
    if (candidateReleasePath) {
        s3Deploy.download candidateReleasePath
        copyFileToEnvironment(selectedEnv, candidateReleasePath, candidateReleasePath, agentWorkspace)
    }
}

private void copyAgileReporterToEnvironment(Map selectedEnv, String candidateReleasePath, String agentWorkspace) {
    if (candidateReleasePath) {
        aws.s3Deploy().agileReporter.download(candidateReleasePath, candidateReleasePath)
        copyFileToEnvironment(selectedEnv, candidateReleasePath, candidateReleasePath, agentWorkspace)
    }
}

private void copyFileToEnvironment(Map selectedEnv, String localPath, String remotePath, String agentWorkspace) {
    String fullRemotePath = "${agentWorkspace}/${selectedEnv.configDir}/${remotePath}"
    String remoteInstallerDirectory = extractDirectoryFromPath(fullRemotePath)

    echo "Copying [$localPath] to [$fullRemotePath] on ${selectedEnv.host}"

    sshagent(credentials: [selectedEnv.credentials]) {
        sh "ssh -o StrictHostKeyChecking=no ${selectedEnv.host} 'hostname'"
        sh "ssh -o StrictHostKeyChecking=no ${selectedEnv.host} 'mkdir -p ${remoteInstallerDirectory}'"
        sh "scp -o StrictHostKeyChecking=no ${localPath} ${selectedEnv.host}:${fullRemotePath}"
    }
}

private String extractDirectoryFromPath(String path) {
    def directoryMatcher = (path =~ /(^.*\/)/);
    if (directoryMatcher.find()) {
        return directoryMatcher[0][1];
    }
    return null;
}