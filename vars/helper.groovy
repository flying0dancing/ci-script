/**
 * get file name,if downloadFileFullName is empty, return null.
 * @param downloadFileFullName
 * @return
 */
def getFileName(downloadFileFullName){
    def downloadFileName
    if(downloadFileFullName){
        if(downloadFileFullName.contains('/')){
            downloadFileName=downloadFileFullName[downloadFileFullName.lastIndexOf('/')+1..-1]
        }else{
            downloadFileName=downloadFileFullName[downloadFileFullName.lastIndexOf('\\')+1..-1]
        }
    }
    return downloadFileName
}

/**
 * get main version by user provided deployment.json->installer's version
 * @param installVer
 * @return if installVer is empty or null or latest, return empty
 */
def getInstallerMainVersion(installVer){
    if(installVer==null || installVer.trim().equals('') || installVer.equalsIgnoreCase('LATEST')){
        installVer=''
    }else{
        installVer=installVer.replaceAll('-(b\\d+|[a-zA-Z]+)','')
    }
    return installVer
}
/**
 * get build number by user provided deployment.json->installer's version(like 'LATEST','19.1.0','19.1.0-b23','19.1.0-SNAPSHOT')
 * @param installVer
 * @return return null if version set to empty or LATEST, return b* or SNAPSHOT if version contains
 */
def getInstallerBuildNumber(installVer){
    def buildNumber

    if(installVer && !(installVer.trim().equals('') || installVer.equalsIgnoreCase('LATEST'))){
        if(installVer.contains('-')){
            buildNumber=installVer.replaceAll('[\\d\\.]+-','')
        }
    }
    return buildNumber
}
/**
 * get version from file name, like 'HKMA_v5.2.0-b96_sign.lrm' return 5.2.0-b96
 * @param downloadFileName
 * @return
 */
def getInstallerRealVersion(downloadFileName){
    def productVerion
    if(downloadFileName){
        productVerion=downloadFileName.replaceAll('(_sign)?\\.[a-z]+','')
        productVerion=productVerion.replaceAll('[a-zA-Z_]+_v','')
    }
    return productVerion
}
/*
println getFileName('AgileREPORTER-19.4.2-b162.jar')
println getFileName('E:\\home\\AgileREPORTER\\19.4.2\\AgileREPORTER-19.4.2-b162.jar')
println getFileName('/home/test/repository/AgileREPORTER/19.4.2/AgileREPORTER-19.4.2-b162.jar')
println getFileName('/home/test/repository/ARProduct/hkma/candidate-release/5.32.0/b70/HKMA_v5.32.0-b70.zip')
println getInstallerMainVersion(null)
println getInstallerMainVersion('   ')
println getInstallerMainVersion('')
println getInstallerMainVersion('LAtEST')
println getInstallerMainVersion('19.1.02')
println getInstallerMainVersion('19.1.02-b23')
println getInstallerMainVersion('19.1.02-SNAPSHOT')
println getInstallerMainVersion('19.1.02-SNaPsHOT')
*/
