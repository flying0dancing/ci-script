def call(productInstaller,projectFolder,propertiesSet){
    def installVer=productInstaller.version
    def buildNumber=helper.getInstallerBuildNumber(installVer)
    def downloadFileFullName
    def downloadFileName
    createHtmlContent('headline',' * ['+productInstaller.prefix+', '+installVer+']')
    createHtmlContent('stepStartFlag')
    if(!productInstaller.needInstall || !productInstaller.needInstall.equalsIgnoreCase("no")){
        echo 'install Product '+productInstaller.prefix+'...'
        installVer=helper.getInstallerMainVersion(installVer)
        downloadFileFullName=searchInstaller.searchLatestProduct(projectFolder,propertiesSet,productInstaller.prefix.toUpperCase(),installVer,buildNumber)
        downloadFileName=helper.getFileName(downloadFileFullName)
        if(downloadFileName){
            def flag=searchInstaller.remoteInstallercheck(propertiesSet,downloadFileName)
            if(flag==0){
                createHtmlContent('stepline','install product: '+downloadFileName)
                installARProduct(projectFolder,propertiesSet,downloadFileFullName,downloadFileName)
            }else{
                echo "no need to install product ["+productInstaller.prefix+", "+installVer+" ]"
                createHtmlContent('stepline','install product: no need, skip')
            }
        }else{
            echo "cannot find install product ["+productInstaller.prefix+", "+installVer+" ]"
            createHtmlContent('stepline','install product: cannot find, skip')
        }


    }else{
        echo "no need to install product ["+productInstaller.prefix+", "+installVer+" ]"
        createHtmlContent('stepline','install product: no need, skip')
    }

    buildNumber=helper.getInstallerRealBuildNumber(downloadFileName,buildNumber)
    def props=productInstaller.props
    if(props){
        for(int j=0;j<props.size();j++){
            echo "=================================config index[${j}]============================================"
            if(!props[j].needConfig || !props[j].needConfig.equalsIgnoreCase("no")){
                def eaFlag='1'
                if(props[j].REPORTERMetadata.equalsIgnoreCase("yes")){
                    echo "config REPORTER metadata ${props[j]}"
                }else{
                    echo "config PRODUCT ${props[j]}"
                    eaFlag='2'
                }
                createHtmlContent('stepline','config '+props[j])
                linkARprojectDID(projectFolder,propertiesSet,productInstaller.prefix,installVer+'-'+buildNumber,props[j].filename,props[j].aliases,eaFlag)
            }else{
                echo "no need to config ${props[j]}"
                createHtmlContent('stepline','config '+props[j]+': no need, skip')
            }
        }
    }
    createHtmlContent('stepEndFlag')
}