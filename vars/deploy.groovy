def call(String projectFolder, String repoName, String repoBranch, String jobName='test_deploy_a', String officeHook='https://outlook.office.com/webhook/faaeef7d-d836-45a5-9b07-0f1f13d0c25b@4a78f2c0-297f-426d-b09f-5986924d38e7/IncomingWebhook/a33daad345b74ad29ecc77c000f42df3/ab2c9d48-0305-4d56-9959-8c7215573a5a'){
    def branches=[:]
    def MAX_CONCURRENT = 2
    //create a fifo
    latch = new java.util.concurrent.LinkedBlockingDeque(MAX_CONCURRENT)
    //put resource in fifo
    for(int i=0; i<MAX_CONCURRENT; i++)
    {latch.offer("$i")}
    //def job_list = ["test1","test2","test3","test4","test5","test6"]
    def job_list=helper.getDeployFolders(projectFolder)
    if(job_list){
        if(job_list.size()==1){
            dInOneJob(job_list[0], projectFolder, officeHook)
        }else{
            for(int i=0; i<job_list.size(); i++) {
                def name = job_list[i]
                branches[name] = {
                    def thing = null
                    waitUntil {
                        thing = latch.pollFirst();
                        return thing != null;
                    }
                    try {
                        //execute job
                        //build(job: name, propagate: false)
                        dTriggerOtherJob(name, projectFolder, repoName, repoBranch, jobName)
                    }finally {
                        //release a resource
                        latch.offer(thing)
                    }
                }
            }
        }
    }
    parallel branches
}


def triggerOtherJob(String projectFolder, String repoName, String repoBranch, String jobName='test_deploy_a'){
    def branches=[:]
    def MAX_CONCURRENT = 2
    //create a fifo
    latch = new java.util.concurrent.LinkedBlockingDeque(MAX_CONCURRENT)
    //put resource in fifo
    for(int i=0; i<MAX_CONCURRENT; i++)
    {latch.offer("$i")}
    //def job_list = ["test1","test2","test3","test4","test5","test6"]
    def job_list=helper.getDeployFolders(projectFolder)

    for(int i=0; i<job_list.size(); i++) {
        def name = job_list[i]
        branches[name] = {
            def thing = null
            waitUntil {
                thing = latch.pollFirst();
                return thing != null;
            }
            try {
                dTriggerOtherJob(name, projectFolder, repoName, repoBranch, jobName)
            }finally {
                //release a resource
                latch.offer(thing)
            }
        }
    }
    parallel branches
}


def inOneJob(String projectFolder, String officeHook='https://outlook.office.com/webhook/faaeef7d-d836-45a5-9b07-0f1f13d0c25b@4a78f2c0-297f-426d-b09f-5986924d38e7/IncomingWebhook/a33daad345b74ad29ecc77c000f42df3/ab2c9d48-0305-4d56-9959-8c7215573a5a'){
    def branches=[:]
    def MAX_CONCURRENT = 2
    //create a fifo
    latch = new java.util.concurrent.LinkedBlockingDeque(MAX_CONCURRENT)
    //put resource in fifo
    for(int i=0; i<MAX_CONCURRENT; i++)
    {latch.offer("$i")}
    //def job_list = ["test1","test2","test3","test4","test5","test6"]
    def job_list=helper.getDeployFolders(projectFolder)

    for(int i=0; i<job_list.size(); i++) {
        def name = job_list[i]
        branches[name] = {
            def thing = null
            waitUntil {
                thing = latch.pollFirst();
                return thing != null;
            }
            try {
                dInOneJob(name, projectFolder, officeHook)
            }finally {
                //release a resource
                latch.offer(thing)
            }
        }
    }
    parallel branches
}



private def dTriggerOtherJob(String deployFolder, String projectFolder, String repoName, String repoBranch, String jobName='test_deploy_a'){
    helper.echoName(deployFolder)
    //TODO backup environment and database
    jobB = build job:jobName , parameters: [string(name: 'REPO_NAME', value:"$repoName"), string(name: 'REPO_BRANCH', value:"$repoBranch"), string(name: 'PROJECT_FOLDER', value: "$projectFolder"), string(name: 'DEPLOY_FOLDER', value: "$deployFolder")]
    println jobB.getResult()
    //TODO clean downloadPath
}
private def dInOneJob(String deployFolder, String projectFolder, String officeHook='https://outlook.office.com/webhook/faaeef7d-d836-45a5-9b07-0f1f13d0c25b@4a78f2c0-297f-426d-b09f-5986924d38e7/IncomingWebhook/a33daad345b74ad29ecc77c000f42df3/ab2c9d48-0305-4d56-9959-8c7215573a5a'){
    helper.echoName(deployFolder)
    //TODO backup environment and database
    installersJson(projectFolder,deployFolder)
    def propsSet=readProperty.get(projectFolder,deployFolder)
    //TODO start service
    officeConnector(propsSet,officeHook)
    //TODO clean downloadPath
}