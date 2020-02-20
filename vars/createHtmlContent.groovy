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
        case 'steplineP1':
            createDisplaySteplineP1(content)
            break
        case 'steplineP2':
            createDisplaySteplineP2(content)
            break
        default:
            createDisplayStepline(content)
    }
}

String createDisplayHeadline(String content,String headsize){
    return updateBuild("""<h${headsize} style='margin-bottom:5px'>${content}</h${headsize}>\n""")
}
String createDisplayStartOl(){
    return updateBuild(""" <ol> \n""")
}
String createDisplayStepline(String content){
    return updateBuild(""" <li>${content}</li> \n""")
}
String createDisplayEndOl(){
    return updateBuild(""" </ol> \n""")
}
def updateBuild(htmlStr){
    currentBuild.description=(currentBuild.description?currentBuild.description:'')+htmlStr
}
String createDisplaySteplineP1(String content){
    return updateBuild(""" <li>${content} \n""")
}
String createDisplaySteplineP2(String content){
    return updateBuild(""" <p style='padding-left:2em'>${content}</p></li> \n""")
}