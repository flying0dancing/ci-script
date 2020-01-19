Boolean downloadFromLocal(props){
    Boolean flag=false
    def defaultUseRepo=props['default.use.repo']
    echo "${defaultUseRepo}"
    if(!defaultUseRepo || defaultUseRepo.equalsIgnoreCase('local')){
        flag=true
    }
    return flag
}