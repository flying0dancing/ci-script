Boolean downloadFromLocal(props){
    Boolean flag=false
    def defaultUseRepo=props['default.use.repo']
    //echo "deployment.properties==>default.use.repo=${defaultUseRepo}"
    if(defaultUseRepo && defaultUseRepo.equalsIgnoreCase('local')){
        flag=true
    }
    return flag
}

def get(projectFolder,deployFolder){
    def propertiesFileName='deployment.properties'
    def propertiesFiles=findFiles(glob: '**/'+projectFolder+'/**/'+deployFolder+'/'+propertiesFileName)
    def propertiesSet=readProperties file: propertiesFiles[0].path
    propertiesSet=helper.resetProps(propertiesSet)
    return propertiesSet
}