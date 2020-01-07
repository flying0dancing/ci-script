import static com.lombardrisk.pipeline.Credentials.AWS

def setup(Map args) {
    withAWS(credentials: AWS, region: 'eu-west-1') {
        addDefaultArgs args

        def response = cfnValidate args
        if (response) {
            echo "Cloud formation template validated: ${response}"
        }

        def argsList = ''
        args.each { argsList += "  $it\n" }
        echo "Deploy cloud formation stack:\n $argsList"

        def outputs = cfnUpdate args
        echo "Cloud formation deployed: ${outputs}"

        return outputs
    }
}

def destroy(Map args) {
    withAWS(credentials: AWS, region: 'eu-west-1') {
        addDefaultArgs args

        cfnDelete args
    }
}

def describe(Map args) {
    withAWS(credentials: AWS, region: 'eu-west-1') {
        def outputs = cfnDescribe stack: args.stack

        echo "CloudFormation stack ${args.stack} outputs: ${outputs}"

        return outputs
    }
}

private void addDefaultArgs(Map args) {
    args.onFailure = 'DO_NOTHING'
    args.pollInterval = 10000
}