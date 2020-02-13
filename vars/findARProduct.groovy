def call(installer,projectName,propertiesSet){
    def iVersion=installer.version
    def iPrefix=installer.prefix
    def needInstall=installer.needInstall

    def buildNumber=helper.getInstallerBuildNumber(iVersion)
    def downloadFileFullName=searchInstaller.searchLatestProduct(projectName,propertiesSet,iPrefix.toUpperCase(),helper.getInstallerMainVersion(iVersion),buildNumber)
    def downloadFileName=helper.getFileName(downloadFileFullName)

    createHtmlContent('headline',' * ['+iPrefix+', '+iVersion+']')
    createHtmlContent('stepStartFlag')
    if(needInstall && needInstall.equalsIgnoreCase("no")){
        echo "no need to install product ["+iPrefix+", "+iVersion+" ]"
        createHtmlContent('stepline','install product: no need, skip')
    }else{
        echo 'install Product '+iPrefix+'...'
        if(downloadFileName){
            def flag=searchInstaller.remoteInstallercheck(propertiesSet,downloadFileName)
            if(flag==0){
                createHtmlContent('stepline','install product: '+downloadFileName)
                installARProduct(projectName,propertiesSet,downloadFileFullName,downloadFileName)
            }else{
                echo "no need to install product ["+iPrefix+", "+iVersion+" ]"
                createHtmlContent('stepline','install product: no need, skip')
            }
        }else{
            echo "cannot find install product ["+iPrefix+", "+iVersion+" ]"
            createHtmlContent('stepline','install product: cannot find, skip')
        }
    }
    def props=installer.props
    handleConfigProps(props,projectName,propertiesSet,iPrefix,downloadFileName)
    createHtmlContent('stepEndFlag')
}

def handleConfigProps(props,projectName,propertiesSet,iPrefix,downloadFileName){
    def installVersion=helper.getInstallerVersion(downloadFileName)
    if(props && installVersion){
        for(int j=0;j<props.size();j++){
            def propy=props[j]
            def needConfig=propy.needConfig
            echo "=================================config index[${j}]============================================"
            if(needConfig && needConfig.equalsIgnoreCase("no")){
                echo "no need to config ${propy}"
                createHtmlContent('stepline','config '+propy+': no need, skip')
            }else{
                def metaData=propy.REPORTERMetadata
                def eaFlag='1' //1 means config argument is -ea
                if(metaData && metaData.equalsIgnoreCase("yes")){
                    echo "config REPORTER metadata ${propy}"
                    eaFlag='2' //2 means config argument is -da and -aa
                }else{
                    echo "config PRODUCT ${propy}"
                }
                createHtmlContent('stepline','config '+propy)

                linkARprojectDID(projectName,propertiesSet,iPrefix,installVersion,propy.filename,propy.aliases,eaFlag)
            }
        }
    }
}