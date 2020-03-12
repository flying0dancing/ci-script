import groovy.transform.Field
//global variable
@Field def ENVS = [
        'test-172.20.31.7' : [
                homeDir  : '/home/test',
                host: 'test@172.20.31.7',
                credentials: 'product-ci-sha-local1-user-test'
        ],
        'test-sha-com-qa-3' : [
                homeDir :   '/home/test',
                host: 'test@172.20.31.7',
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
                host : 'oracle@172.20.20.49',
                credentials: 'product-ci-sha-db1-user-oracle'
        ],
        'test-172.20.30.89' : [
                homeDir  : '/home/test',
                host: 'test@172.20.30.89',
                credentials: 'product-ci-sha-local2-user-test'
        ],
        'test-sha-prod-001' : [
                homeDir :   '/home/test',
                host: 'test@172.20.30.89',
                credentials: 'product-ci-sha-local2-user-test'
        ],
        'test-172.20.31.38' : [
                homeDir :   '/home/test',
                host: 'test@172.20.31.38',
                credentials: 'product-ci-sha-local-qa11-test'
        ],
        'test-sha-com-qa-11' : [
                homeDir :   '/home/test',
                host: 'test@172.20.31.38',
                credentials: 'product-ci-sha-local-qa11-test'
        ],
        'test-172.20.30.167' : [
                homeDir :   '/home/test1',
                host: 'test1@172.20.30.167',
                credentials: 'product-ci-sha-local-qa7-test1'
        ],
        'test-sha-com-qa-7' : [
                homeDir :   '/home/test1',
                host: 'test1@172.20.30.167',
                credentials: 'product-ci-sha-local-qa7-test1'
        ],
        'test-172.20.30.228' : [
                homeDir :   '/home/test',
                host: 'test@172.20.30.228',
                credentials: 'product-ci-sha-local-qa9-test'
        ],
        'test-sha-com-qa-9' : [
                homeDir :   '/home/test',
                host: 'test@172.20.30.228',
                credentials: 'product-ci-sha-local-qa9-test'
        ],
        'test-172.20.31.36' : [
                homeDir :   '/home/test',
                host: 'test@172.20.31.36',
                credentials: 'product-ci-sha-local-qa10-test'
        ],
        'test-sha-com-qa-10' : [
                homeDir :   '/home/test',
                host: 'test@172.20.31.36',
                credentials: 'product-ci-sha-local-qa10-test'
        ],
]

/**
 * environments
 * @return
 */
Map get(String label){
    def SELECTED_ENV = [:]
    if(label){
        SELECTED_ENV=ENVS[label]
    }
    return SELECTED_ENV
}

void check(String envLabel, Map selectedEnv) {
    if (!selectedEnv) {
        error "Parameter 'envLabel' must not be blank"
    }
    echo "Deploy to environment ${envLabel}:${selectedEnv}"
}

def get(String user, String host,String credentials=null){
    def envLabel="${user}-${host}"
    Map selectedEnv=get(envLabel)
    if(!selectedEnv){
        selectedEnv=[:]
        selectedEnv.homeDir="/home/${user}"
        selectedEnv.host="${user}@${host}"
        selectedEnv.credentials=credentials

    }
    return selectedEnv
}

def get(Map props){
    String user=props['app.user']
    String host=props['app.host']
    String credentials=props['app.jenkins.credentials']
    return get(user, host, credentials)
}
/*
println get('test-sha-com-qa-11')
println get('test','sha-com-qa-12','sha-com-qa-12sssss')
println get('test','sha-com-qa-14')
println get(['app.user':'test1','app.host':'1234','app.jenkins.credentials':'aaaaaaaa'])
println get(['app.user':'test','app.host':'sha-com-qa-3','app.jenkins.credentials':'aaaaaaaa'])
 */
