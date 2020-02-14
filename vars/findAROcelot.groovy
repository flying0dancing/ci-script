def call(installer,projectName,propertiesSet){
    def iVersion=installer.version
    def iPrefix=installer.prefix
    def needInstall=installer.needInstall
    createHtmlContent('headline',' * ['+iPrefix+', '+iVersion+']')
    createHtmlContent('stepStartFlag')
    if(needInstall && needInstall.equalsIgnoreCase("no")){
        echo "no need to install ["+iPrefix+", "+iVersion+" ]"
        createHtmlContent('stepline','install ocelot: no need, skip')
    }else{
        echo 'install '+iPrefix+'...'
        def downloadFileFullName=searchInstaller.searchLatestOcelot(propertiesSet,iPrefix,helper.getInstallerMainVersion(iVersion),helper.getInstallerBuildNumber(iVersion))
        def downloadFileName=helper.getFileName(downloadFileFullName)

        echo 'downloadFileFullName:'+downloadFileFullName
        echo 'downloadFileName:'+downloadFileName
        if(downloadFileName){
            //InstallerCheck and installOcelot
            def flag=searchInstaller.remoteInstallercheck(propertiesSet,downloadFileName)
            if(flag==0){
                createHtmlContent('stepline','install ocelot: '+downloadFileName)
                def props=installer.props
                if(props){
                    echo props[0].filename
                    opAROcelot(projectName,propertiesSet,downloadFileFullName,downloadFileName,props[0].filename)
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