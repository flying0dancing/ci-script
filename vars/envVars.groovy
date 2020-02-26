/**
 * environments
 * @return
 */
Map get(String label){
    def ENVS = [
            'test-172.20.31.7' : [
                    homeDir  : '/home/test',
                    host: 'test@172.20.31.7',
                    credentials: 'product-ci-sha-local1-user-test'
            ],
            'test-sha-com-qa-3' : [
                    homeDir :   '/home/test',
                    host: 'test@sha-com-qa-3',
                    credentials: 'product-ci-sha-local1-user-test'
            ],
            'oracle-172.20.20.49' : [
                    homeDir  : '/home/oracle',
                    configDir: 'impdp_and_expdp_shell/',
                    host : 'oracle@172.20.20.49',
                    credentials: 'product-ci-sha-db1-user-oracle'
            ],
            'oracle-sha-oracle-01' : [
                    homeDir  : '/home/oracle',
                    configDir: 'impdp_and_expdp_shell/',
                    host : 'oracle@sha-oracle-01',
                    credentials: 'product-ci-sha-db1-user-oracle'
            ]
    ]
    def SELECTED_ENV = [:]
    return SELECTED_ENV=ENVS[label]
}

void check(String envLabel, Map selectedEnv) {
    if (!selectedEnv) {
        error "Parameter 'ENV_LABEL' must not be blank"
    }
    echo "Release to environment ${envLabel}:${selectedEnv}"
}