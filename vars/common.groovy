void setupEnvVars() {
    env.TEAMS_CHANNEL_URL = 'https://outlook.office.com/webhook/5293372d-2a82-4815-810f-6277508c391d@4a78f2c0-297f-426d-b09f-5986924d38e7/JenkinsCI/cb6b51e58c454df9b04b8c76b4a4d901/624846db-45e7-4d00-924f-d0dd4703b032'

    env.JENKINS_NODE_COOKIE = 'dontKillMe'
}

void removeOldReleases(String rootDir) {
    def currentVersions = quietSh(script: "ls -t ${rootDir}", returnStdout: true).split(/\s+/)
    def latestVersions = currentVersions.take(3)

    def versionsToRemove = (currentVersions - latestVersions).join(' ')

    echo "Removing ${versionsToRemove} and keeping ${latestVersions} versions only in [${rootDir}]"

    if (!versionsToRemove.isEmpty()) {
        quietSh "cd ${rootDir} && rm -rf ${versionsToRemove}"
    }
    return versionsToRemove
}

boolean isExisting(String path) {
    def exitCode = quietSh script: "ls ${path}", returnStatus: true

    return exitCode == 0
}