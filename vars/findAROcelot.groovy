def call(installer,projectName,propertiesSet){
    def iVersion=helper.removeV(installer.version)
    def iPrefix=installer.prefix
    def needInstall=installer.needInstall
    def mainVersion=helper.getInstallerMainVersion(iVersion)
    def buildNumber=helper.getInstallerBuildNumber(iVersion)
    echo '=============================== install '+iPrefix+' =================================='
    createHtmlContent('headline',' * ['+iPrefix+', '+iVersion+']')
    createHtmlContent('stepStartFlag')
    if(needInstall && needInstall.equalsIgnoreCase("no")){
        echo "no need to install ["+iPrefix+", "+iVersion+" ]"
        createHtmlContent('stepline','install ocelot: no need, skip')
    }else{
        def downloadFileFullName=searchInstaller.searchLatestOcelot(propertiesSet,iPrefix,mainVersion,buildNumber)
        def downloadFileName=helper.getFileName(downloadFileFullName)

        echo 'downloadFileFullName:'+downloadFileFullName
        echo 'downloadFileName:'+downloadFileName
        if(downloadFileName){
            //InstallerCheck and installOcelot
            def flag=searchInstaller.checkNeedInstallOrNot(propertiesSet,downloadFileName)
            if(flag==0){
                createHtmlContent('stepline','install ocelot: '+downloadFileName)
                def downloadFileFullName1
                if(!readProperty.downloadFromLocal(propertiesSet) && searchInstaller.existsInLocal(propertiesSet,downloadFileFullName)!=0){
                    downloadFileFullName1=helper.removeBuildFolder(downloadFileFullName)
                    if(searchInstaller.existsInLocal(propertiesSet,downloadFileFullName1)==0){
                        downloadFileFullName=downloadFileFullName1
                        echo 'new downloadFileFullName:'+downloadFileFullName
                    }else{
                        downloadFileFullName=searchInstaller.searchLatestOcelot(propertiesSet,iPrefix,mainVersion,buildNumber,true)
                    }
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