void call(Map args) {
    echo "Checking Ignis Server is healthy ${args}"

    retry(10) {
        checkHealth(args.host, args.port)
    }
}

private void checkHealth(host, port) {
    sleep 15

    def statusJson = quietSh script: """
        curl --silent --fail --show-error \
             --request GET \
             --insecure \
             --header 'Content-Type: application/json' \
             https://${host}:${port}/fcrengine/actuator/health""",
            returnStdout: true

    def expectedStatus = 'UP'
    if (!statusJson.contains("\"status\":\"$expectedStatus\"")) {
        error "Ignis Server did not start up correctly. Health: $statusJson"
    }
}