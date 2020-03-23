def call(installer,projectName,propertiesSet){
    def iVersion=helper.removeV(installer.version)
    def iPrefix=installer.prefix
    def needInstall=installer.needInstall
    def mainVersion=helper.getInstallerMainVersion(iVersion)
    def buildNumber=helper.getInstallerBuildNumber(iVersion)
    def downloadFileFullName
    def downloadFileName
    echo '=============================== install '+iPrefix+' =================================='
    createHtmlContent('headline',' * ['+iPrefix+', '+iVersion+']')
    createHtmlContent('stepStartFlag')
    if(needInstall && needInstall.equalsIgnoreCase("no")){
        echo "no need to install product ["+iPrefix+", "+iVersion+" ]"
        createHtmlContent('stepline','install product: no need, skip')
    }else{
        downloadFileFullName=searchInstaller.searchLatestProduct(propertiesSet,iPrefix.toUpperCase(),mainVersion,buildNumber)
        downloadFileName=helper.getFileName(downloadFileFullName)

        echo 'downloadFileFullName:'+downloadFileFullName
        echo 'downloadFileName:'+downloadFileName
        if(downloadFileName){
            def flag=searchInstaller.checkNeedInstallOrNot(propertiesSet,downloadFileName)
            if(flag==0){
                createHtmlContent('stepline','install product: '+downloadFileName)
                if(!readProperty.downloadFromLocal(propertiesSet) && searchInstaller.existsInLocal(propertiesSet,downloadFileFullName)!=0){
                    def downloadFileFullName1=searchInstaller.searchProductFromLocal(propertiesSet,downloadFileName)
                    if(downloadFileFullName1){
                        downloadFileFullName=downloadFileFullName1
                        echo 'new downloadFileFullName:'+downloadFileFullName
                    }else{
                        downloadFileFullName=searchInstaller.searchLatestProduct(propertiesSet,iPrefix.toUpperCase(),mainVersion,buildNumber,true)
                    }
                }
                installARProduct(propertiesSet,downloadFileFullName)
            }else{
                echo "no need to install ["+iPrefix+", "+iVersion+"], provided version lower"
                createHtmlContent('stepline','install product: provided version lower, skip')
            }
        }else{
            echo "cannot find install product ["+iPrefix+", "+iVersion+" ]"
            createHtmlContent('stepline','install product: cannot find, skip')
        }
    }
    if(installer.props){
        if(!downloadFileName){
            downloadFileFullName=searchInstaller.searchLatestProduct(propertiesSet,iPrefix.toUpperCase(),mainVersion,buildNumber)
            downloadFileName=helper.getFileName(downloadFileFullName)
        }
        if(downloadFileName){
            handleConfigProps(installer.props,projectName,propertiesSet,iPrefix,downloadFileName)
        }
    }else{
        createHtmlContent('stepline','config product: no need, skip')
    }

    createHtmlContent('stepEndFlag')
}

def handleConfigProps(props,projectName,propertiesSet,iPrefix,downloadFileName){
    def installVersion=helper.getInstallerRealVersion(downloadFileName)
    for(int j=0;j<props.size();j++){
        def propy=props[j]
        def needConfig=propy.needConfig
        echo "========= DID configure $propy.filename ========="
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
            createHtmlContent('steplineP1','config '+propy)
            linkARProductDID(projectName,propertiesSet,iPrefix,installVersion,propy,eaFlag)
        }
    }
}