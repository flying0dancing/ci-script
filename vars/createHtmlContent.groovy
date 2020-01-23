def call(String expression,String content='',String headSize='4'){
    switch(expression){
        case 'headline':
            createDisplayHeadline(content, headSize)
            break
        case 'stepline':
            createDisplayStepline(content)
            break
        case 'stepStartFlag':
            createDisplayStartOl()
            break
        case 'stepEndFlag':
            createDisplayEndOl()
            break
        default:
            createDisplayStepline(content)
    }
}

String createDisplayHeadline(String content,String headsize){
    return updateBuild("""<h${headsize} style='margin-bottom:5px'>${content}</h${headsize}>""")
}
String createDisplayStartOl(){
    return updateBuild(""" <ol> """)
}
String createDisplayStepline(String content){
    return updateBuild(""" <li>${content}</li> """)
}
String createDisplayEndOl(){
    return updateBuild(""" </ol> """)
}
def updateBuild(htmlStr){
    currentBuild.description=(currentBuild.description?currentBuild.description:'')+htmlStr
}