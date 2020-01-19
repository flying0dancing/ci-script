/**
 * get file name,if downloadFileFullName is empty, return empty.
 * @param downloadFileFullName
 * @return
 */
def getFileName(downloadFileFullName){
    def downloadFileName
    if(downloadFileFullName && downloadFileFullName.contains('/')){
        downloadFileName=downloadFileFullName[downloadFileFullName.lastIndexOf('/')+1]
    }
    return downloadFileName
}

/**
 * get main version by user provided deployment.json->installer's version
 * @param installVer
 * @return
 */
def getInstallerMainVersion(installVer){
    if(installVer==null || installVer.equalsIgnoreCase('LATEST')){
        installVer=''
    }else{
        if(installVer.contains('-b')){
            installVer=installVer[0..installVer.indexOf('-b')-1]
        }
        if(installVer.toUpperCase().contains('-SNAPSHOT')){
            installVer=installVer.toUpperCase()
            installVer=installVer[0..installVer.indexOf('-SNAPSHOT')-1]
        }
    }
    return installVer
}
/**
 * get build number by user provided deployment.json->installer's version
 * @param installVer
 * @return
 */
def getInstallerBuildNumber(installVer){
    def buildNumber
    if(installVer!=null && !installVer.equals('')){
        if(installVer.contains('-b')){
            buildNumber=installVer[installVer.indexOf('b')..-1]
        }
        if(installVer.toUpperCase().contains('-SNAPSHOT')){
            buildNumber='SNAPSHOT'
        }
    }
    return buildNumber
}

/**
 * get build number of product installer name
 * @param downloadFileName
 * @param buildNumber
 * @return
 */
def getInstallerRealBuildNumber(downloadFileName,buildNumber){
    if(downloadFileName){
        if(buildNumber.indexOf('-b')!=-1){
            buildNumber=downloadFileName[downloadFileName.indexOf('b')..-5]
        }
        if(buildNumber.indexOf('-SNAPSHOT')!=-1){
            buildNumber='SNAPSHOT'
        }
        if(buildNumber.indexOf('_sign')!=-1){
            buildNumber=buildNumber[0..buildNumber.indexOf('_sign')-1]
        }
    }
    return buildNumber
}