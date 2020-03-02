Boolean downloadFromLocal(props){
    Boolean flag=false
    def defaultUseRepo=props['default.use.repo']
    //echo "env.properties==>default.use.repo=${defaultUseRepo}"
    if(defaultUseRepo && defaultUseRepo.equalsIgnoreCase('local')){
        flag=true
    }
    return flag
}

def getProps(projectFolder,deployFolder){
    def propertiesFileName='env.properties'
    def propertiesFiles=findFiles(glob: '**/'+projectFolder+'/**/'+deployFolder+'/'+propertiesFileName)
    def propertiesSet=readProperties file: propertiesFiles[0].path
    propertiesSet=helper.resetProps(propertiesSet)
    return propertiesSet
}