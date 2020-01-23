def call(String projectFolder){
    def branches=[:]
    def MAX_CONCURRENT = 2
    //create a fifo
    latch = new java.util.concurrent.LinkedBlockingDeque(MAX_CONCURRENT)
    //put resource in fifo
    for(int i=0; i<MAX_CONCURRENT; i++)
    {latch.offer("$i")}
    //def job_list = ["test1","test2","test3","test4","test5","test6"]
    def job_list=getSubFolders(projectFolder)

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
                echoName(name)
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

def echoName(String name){
    echo "execute: ${name}......."
}

def getSubFolders(projectFolder){
    def allFolders=[]
    dir(projectFolder+'/src/main/resources'){
        allFolders=sh(returnStdout: true, script: '''ls -l|grep "^d"|awk '{ print $NF }' ''').trim().split()
        echo "all subfolders: ${allFolders}"
    }
    allFolders=getValidFolders(allFolders)

    return allFolders
}

def getValidFolders(folders){
    def allFolders=[]
    for(int i=0;i<folders.size();i++){
        if(findFiles(glob: '**/'+folders[i]+'/deployment.json')){
            allFolders+=folders[i]
        }
    }
    echo "valid folders: ${allFolders}"
    return allFolders
}

def installersJson(projectFolder,deployFolder){

    def jsonFileName='deployment.json'
    def propertiesFileName='env.properties'
    def files=findFiles(glob: '**/'+projectFolder+'/**/'+deployFolder+'/'+jsonFileName)
    def propertiesFiles=findFiles(glob: '**/'+projectFolder+'/**/'+deployFolder+'/'+propertiesFileName)
    def propertiesSet=readProperties file: propertiesFiles[0]
    def gado
    def installers
    def installPrefix
    gado=readJSON file: files[0].path
    installers=gado.installers
    if(installers){
        copyScripts(propertiesSet)
        createHtmlContent('headline','Install Ocelot and Products','3')
        for(int i=0;i<installers.size();i++){
            installPrefix=installers[i].prefix

            if(installPrefix.equalsIgnoreCase('AgileREPORTER')){
                installers[i].prefix='AgileREPORTER'
                findAROcelot(installers[i],projectFolder,propertiesSet)
            }else{
                findARProduct(installers[i],projectFolder,propertiesSet)
            }

        }
    }else{
        error "cannot found install products in gado.json"
    }

}