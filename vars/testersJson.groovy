def call(workspace,projectFolder,deployFolder, destParentDir="c:\\ar_auto"){
    copyTrump(workspace,projectFolder,deployFolder,destParentDir)
    def jsonFileName='autotest.json'
    def files=findFiles(glob: '**/'+projectFolder+'/**/'+deployFolder+'/'+jsonFileName)
    def gado
    def testers
    def xmlFileName
    def resultFolder
    def srcFolder
    def resultParent
    def resultName
    def suffix=helper.today()
    gado=readJSON file: files[0].path
    testers=gado.testers
    if(testers){
        //createHtmlContent('headline','Install Ocelot and Products','3')
        for(int i=0;i<testers.size();i++){
            srcFolder="$workspace\\$projectFolder\\src\\test\\resources"
            xmlFileName="$workspace\\$projectFolder\\src\\test\\resources\\scenarios\\${testers[i].xmlFileName}"
            //get result folder, if exists, generate a new folder name with YYYYmmdd like this
            resultParent=helper.getFilePath(testers[i].resultFolder)
            resultName=helper.getFileName(testers[i].resultFolder)
            resultFolder=bat(returnStdout: true, script: "$workspace\\scripts\\getNewFullName.bat $resultParent $resultName $suffix").trim()

            echo "mvn test -DxmlFileName=$xmlFileName -DsrcFolder=$srcFolder -DresultFolder=$resultFolder"
            autoTest(xmlFileName,srcFolder,resultFolder, destParentDir)

        }
    }else{
        error "cannot found testers in $jsonFileName"
    }

}

void autoTest(xmlFileName, srcFolder, resultFolder, destParentDir="c:\\ar_auto"){
    /*def mvn = maven.initialiseMvn()
    def build_info = maven.newBuildInfo()
    mvn.deployer.deployArtifacts = false // Disable artifacts deployment during Maven run

    dir(projectFolder){
        mvn.run pom: 'pom.xml', goals: 'clean test -DxmlFileName='+xmlFileName+' -DsrcFolder='+srcFolder+' -DresultFolder='+resultFolder, buildInfo: build_info
    }
    */
    dir(destParentDir){
        //bat(returnStatus: true, script: "RunTest.bat $xmlFileName $srcFolder $resultFolder")
        bat(returnStatus: true, script: "RunTest1.bat $xmlFileName $srcFolder $resultFolder")
    }

}