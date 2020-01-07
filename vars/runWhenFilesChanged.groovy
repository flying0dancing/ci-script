void call(String rootPath, Closure body) {
    def changedFiles = findChanges(rootPath)

    if (!changedFiles.isEmpty()) {
        echo "Found file changes under [${rootPath}]: \n${changedFiles}"

        body()
    }
}

def findChanges(String rootPath) {
    def changes = quietSh script: 'git diff --name-only $(git describe --abbrev=0)', returnStdout: true

    return changes.split()
            .findAll { it.startsWith(rootPath) }
            .collect { "  $it" }
            .join("\n")
}