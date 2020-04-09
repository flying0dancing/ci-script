def call(workspace,projectFolder,deployFolder, destFolder="c:\\ar_auto\\trump"){
    copyTrump(workspace,projectFolder,deployFolder,destFolder)
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
            dir("$workspace\\scripts"){
                resultFolder=bat(returnStdout: true, script: "@getNewFullName.bat $resultParent $resultName $suffix").trim()
            }
            echo "resultFolder:$resultFolder"
            echo "mvn test -DxmlFileName=$xmlFileName -DsrcFolder=$srcFolder -DresultFolder=$resultFolder"
            autoTest(projectFolder, xmlFileName,srcFolder,resultFolder, destFolder)

        }
    }else{
        error "cannot found testers in $jsonFileName"
    }

}

void autoTest(projectFolder, xmlFileName, srcFolder, resultFolder, destFolder="c:\\ar_auto\\trump"){
    def regulator
    switch(projectFolder){
        case ['dpb','hkma']:
            regulator='hongkongmonetaryauthority'
            break
        case 'mas':
            regulator='monetaryauthorityofsingapore'
            break
        case 'srdd':
            regulator='statsandregulatorydatadiv'
            break
        case 'eba':
            regulator='europeancommonreporting'
            break
        case 'pra':
            regulator='prudentialregulationauthority'
            break
        default:
            regulator='autotest'
    }
    dir(destFolder){
        bat(returnStatus: true, script: "@RunTest.bat $xmlFileName $srcFolder $resultFolder $regulator")
    }

}