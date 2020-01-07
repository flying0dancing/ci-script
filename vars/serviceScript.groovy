void runAndVerifyUp(String script) {
    runAndVerifyNotStatus(script, "Down")
}

void runAndVerifyDown(String script) {
    runAndVerifyNotStatus(script, "Up")
}

private void runAndVerifyNotStatus(String script, String badStatus) {
    sleep 20

    def serviceStatuses = sh script: script, returnStdout: true
    def failedServices =
            serviceStatuses.split('\n')
                    .findAll { it.contains("|$badStatus") }

    if (!failedServices.isEmpty()) {
        error "Found services that are still $badStatus: \n${failedServices.join('\n')}"
    }
}