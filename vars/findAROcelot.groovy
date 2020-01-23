def call(ocelotInstaller,projectFolder,propertiesSet){
    def installVer=ocelotInstaller.version
    createHtmlContent('headline',' * ['+ocelotInstaller.prefix+', '+installVer+']')
    createHtmlContent('stepStartFlag')
    if(!ocelotInstaller.needInstall || !ocelotInstaller.needInstall.equalsIgnoreCase("no")){
        echo 'install '+ocelotInstaller.prefix+'...'
        def downloadFileFullName=searchInstaller.searchLatestOcelot(propertiesSet,ocelotInstaller.prefix,helper.getInstallerMainVersion(installVer),helper.getInstallerBuildNumber(installVer))
        def downloadFileName=helper.getFileName(downloadFileFullName)
        if(downloadFileName){
            //InstallerCheck and installOcelot
            def flag=searchInstaller.remoteInstallercheck(propertiesSet,downloadFileName)
            if(flag==0){
                createHtmlContent('stepline','install ocelot: '+downloadFileName)
                def props=ocelotInstaller.props
                if(props){
                    echo props[0].filename
                    opAROcelot(projectFolder,propertiesSet,downloadFileFullName,downloadFileName,props[0].filename)
                }
            }else{
                echo "no need to install ["+ocelotInstaller.prefix+", "+installVer+" ]"
                createHtmlContent('stepline','install ocelot: no need, skip')
            }
        }else{
            echo "cannot find install ocelot["+ocelotInstaller.prefix+", "+installVer+" ]"
            createHtmlContent('stepline','install ocelot: cannot find, skip')
        }

    }else{
        echo "no need to install ["+ocelotInstaller.prefix+", "+installVer+" ]"
        createHtmlContent('stepline','install ocelot: no need, skip')
    }
    createHtmlContent('stepEndFlag')
}