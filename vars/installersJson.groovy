def call(projectFolder,deployFolder){

    def jsonFileName='deployment.json'
    def propertiesFileName='deployment.properties'
    def files=findFiles(glob: '**/'+projectFolder+'/**/'+deployFolder+'/'+jsonFileName)
    def propertiesFiles=findFiles(glob: '**/'+projectFolder+'/**/'+deployFolder+'/'+propertiesFileName)
    def propertiesSet=readProperties file: propertiesFiles[0].path
    propertiesSet=helper.resetProps(propertiesSet)
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