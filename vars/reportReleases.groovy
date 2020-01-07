void call(String... candidateReleases) {
    def candidateReleaseLinks =
            candidateReleases
                    .findAll { !it.isEmpty() }
                    .collect { s3Deploy.createLink(it) }
                    .collect { "<li>$it</li>" }
                    .join('\n')
    def description = "<ul>Released\n ${candidateReleaseLinks} </ul>"

    if (currentBuild.description) {
        currentBuild.description += description
    } else {
        currentBuild.description = description
    }
}