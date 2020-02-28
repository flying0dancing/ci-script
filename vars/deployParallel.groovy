def call(String projectFolder){
    def branches=[:]
    def MAX_CONCURRENT = 2
    //create a fifo
    latch = new java.util.concurrent.LinkedBlockingDeque(MAX_CONCURRENT)
    //put resource in fifo
    for(int i=0; i<MAX_CONCURRENT; i++)
    {latch.offer("$i")}
    //def job_list = ["test1","test2","test3","test4","test5","test6"]
    def job_list=helper.getSubFolders(projectFolder)

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
                helper.echoName(name)
                //TODO backup environment and database
                installersJson(projectFolder,name)
                //TODO clean downloadPath
            }finally {
                //release a resource
                latch.offer(thing)
            }
        }
    }
    parallel branches
}


