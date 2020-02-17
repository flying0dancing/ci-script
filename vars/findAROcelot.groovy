def call(installer,projectName,propertiesSet){
    def iVersion=installer.version
    def iPrefix=installer.prefix
    def needInstall=installer.needInstall
    def mainVersion=helper.getInstallerMainVersion(iVersion)
    def buildNumber=helper.getInstallerBuildNumber(iVersion)
    createHtmlContent('headline',' * ['+iPrefix+', '+iVersion+']')
    createHtmlContent('stepStartFlag')
    if(needInstall && needInstall.equalsIgnoreCase("no")){
        echo "no need to install ["+iPrefix+", "+iVersion+" ]"
        createHtmlContent('stepline','install ocelot: no need, skip')
    }else{
        echo 'install '+iPrefix+'...'
        def downloadFileFullName=searchInstaller.searchLatestOcelot(propertiesSet,iPrefix,mainVersion,buildNumber)
        def downloadFileName=helper.getFileName(downloadFileFullName)

        echo 'downloadFileFullName:'+downloadFileFullName
        echo 'downloadFileName:'+downloadFileName
        if(downloadFileName){
            //InstallerCheck and installOcelot
            def flag=searchInstaller.checkNeedInstallOrNot(propertiesSet,downloadFileName)
            if(flag==0){
                createHtmlContent('stepline','install ocelot: '+downloadFileName)
                if(!readProperty.downloadFromLocal(props)){
                    downloadFileFullName=searchInstaller.searchLatestOcelot(propertiesSet,iPrefix,mainVersion,buildNumber,true)
                }
                def props=installer.props
                if(props){
                    echo props[0].filename
                    opAROcelot(projectName,propertiesSet,downloadFileFullName,props[0].filename)
                }
            }else{
                echo "no need to install ["+iPrefix+", "+iVersion+" ]"
                createHtmlContent('stepline','install ocelot: no need, skip')
            }
        }else{
            echo "cannot find install ocelot["+iPrefix+", "+iVersion+" ]"
            createHtmlContent('stepline','install ocelot: cannot find, skip')
        }
    }
    createHtmlContent('stepEndFlag')
}