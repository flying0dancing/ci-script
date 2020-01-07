def designStudio(String version, boolean requestInput) {
    def globVersion = version ?: '*'
    return showSelection("design-studio/**/fcr-engine-design-studio-${globVersion}.zip", requestInput)
}

def platformTools(String version, boolean requestInput) {
    def globVersion = version ?: '*'
    return showSelection("platform-tools/**/fcr-engine-platform-tools-${globVersion}.zip", requestInput)
}

def ignisServer(String version, boolean requestInput) {
    def globVersion = version ?: '*'
    return showSelection("ignis-server/**/fcr-engine-ignis-server-${globVersion}.zip", requestInput)
}

def ignisEraser(String version, boolean requestInput) {
    def globVersion = version ?: '*'
    return showSelection("ignis-eraser/**/fcr-engine-ignis-eraser-${globVersion}.zip", requestInput)
}

def agileReporter(String version, boolean requestInput) {
    def globVersion = version ?: '[0-9]*'
    return showSelection("**/AgileREPORTER-${globVersion}.jar", 'agileReporter', requestInput)
}

private def showSelection(String candidateReleaseGlob, String productName = 'fcrEngine', boolean requestInput) {
    if (!requestInput) {

        def files = aws.s3Deploy()
                .get(productName)
                .findFiles(candidateReleaseGlob)
        echo "Found files $files"

        return files[0].path

    } else {
        timeout(time: 2, unit: 'MINUTES') {
            def selectedRelease = createChoiceDialogForS3Deploy(candidateReleaseGlob, productName)

            echo "Select [$selectedRelease] to release"
            return selectedRelease
        }
    }
}


private String createChoiceDialogForS3Deploy(String glob, String productName) {
    def files = aws.s3Deploy()
            .get(productName)
            .findFiles(glob)

    def choice = input([
            message   : 'Choose a candidate release from S3 to release',
            ok        : 'Select',
            parameters: [
                    choice(
                            choices: files.collect { toChoice(it) }.sort().reverse().join('\n'),
                            description: 'Candidate releases available in S3',
                            name: 'Select a file'
                    ),
            ]
    ])

    echo "Selected choice is ${choice}"

    def file = files.find { toChoice(it) == (choice) }

    return file.path
}

private String toChoice(fileWrapper) {
    def type = fileWrapper.directory ? 'Dir' : 'File'
    def lastModifiedDate = new Date(fileWrapper.lastModified).format('yyyy-MM-dd hh:mm:ss')

    "$lastModifiedDate - ${fileWrapper.name} ($type)"
}
