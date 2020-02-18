
/**
 * package all products in projectFolder, packageBuildNumber like b1,b2,b3...,b290,..
 */
void packageARProduct(projectFolder,packageBuildNumber){
    def mvn = maven.initialiseMvn()
    def build_info = maven.newBuildInfo()
    mvn.deployer.deployArtifacts = false // Disable artifacts deployment during Maven run

	dir(projectFolder){
		mvn.run pom: 'pom.xml', goals: 'clean package -U -DskipITs -DskipTests -Dproduct.build.number='+packageBuildNumber, buildInfo: build_info
	}
}

def getProps(projectFolder){
    def files =findFiles(glob: '**/'+projectFolder+'/package.properties')
    def props
    if(files){
        props = readProperties interpolate: true, file: files[0].path
    }
    return props
}

def getExternalProjsFromJson(projectFolder){
    def files =findFiles(glob: '**/'+projectFolder+'/**/testudo.json')
    def returnArr=[]
    def cisettings
    def zipset
    def externalProjs
    for(int index=0;index<files.size();index++){
        cisettings = readJSON file: files[index].path
        for(int i=0;i<cisettings.size();i++){
            zipset=cisettings[i].zipSettings
            if(zipset){
                //println(zipset)
                externalProjs=zipset.externalProjects
                if(externalProjs){
                    for(int j=0;j<externalProjs.size();j++){
                        def flag=returnArr.find{it->it==externalProjs[j].project}
                        //println("${externalProjs[j].project}")
                        if(!flag){
                            returnArr+=externalProjs[j].project
                        }

                    }
                }

            }

        }
    }
    println "external projects:${returnArr}"
    return returnArr
}

void checkoutARProduct(projectFolder){
    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CleanBeforeCheckout'], [$class: 'RelativeTargetDirectory', relativeTargetDir: "${projectFolder}"]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '46afdff1-cdd3-4098-b8af-d904b4d298aa', url: "ssh://git@bitbucket.lombardrisk.com:7999/cprod/${projectFolder}.git"]]])

    def externalProjs=getExternalProjsFromJson(projectFolder)
    if(externalProjs){
        for(int i=0;i<externalProjs.size();i++){
            //checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CleanBeforeCheckout'], [$class: 'RelativeTargetDirectory', relativeTargetDir: "${projectFolder}xbrl"]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '46afdff1-cdd3-4098-b8af-d904b4d298aa', url: "ssh://git@bitbucket.lombardrisk.com:7999/cprod/${externalProjs[i]}.git"]]])
            checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CleanBeforeCheckout'], [$class: 'RelativeTargetDirectory', relativeTargetDir: "${externalProjs[i]}"]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '46afdff1-cdd3-4098-b8af-d904b4d298aa', url: "ssh://git@bitbucket.lombardrisk.com:7999/cprod/${externalProjs[i]}.git"]]])
        }
    }
}




String getProductVersionFolder(projectName){
    def version_ARProduct_Package = sh (
            script: '''awk -F '[<>]' /implementationVersion/'{print $3}' '''+projectName+'''/'''+projectName+'/src/manifest.xml',
            returnStdout: true
    ).trim()
    def versionPattern='(\\d+\\.){2,}\\d+'
    def versionMatcher=(version_ARProduct_Package=~versionPattern)
    versionMatcher.matches()
    assert version_ARProduct_Package=~versionPattern
    version_APRroduct_Pacakge=''+versionMatcher[0][0]
    versionMatcher=null
    echo "main version of ar product package: ${version_ARProduct_Package}"
    return version_ARProduct_Package
}

String getVersionOfARProductFromManifest(manifestFullPath){
    def version_ARProduct_Package = sh (
            script: '''awk -F '[<>]' /implementationVersion/'{print $3}' '''+manifestFullPath,
            returnStdout: true
    ).trim()
    echo "version of ar product pacakge: ${version_ARProduct_Package}"
    return version_ARProduct_Package
}

void uploadARProduct(projectFolder,packageBuildNumber){
    def arProduct_Manifest='/src/manifest.xml'
    def productVersionFolder=getProductVersionFolder(projectFolder)
    def props=getProps(projectFolder)
    def s3_bucket=props['s3.bucket']
    def local_linux=props['local.linux']
    def arproduct_repo_linux=props['arproduct.repo.linux']+projectFolder+'/candidate-release/'
    def manifestFiles = findFiles(glob: '**/'+projectFolder+'/*/target/**'+arProduct_Manifest)
    productVersionFolder=productVersionFolder+'/'+packageBuildNumber
    if(manifestFiles){
        for(int festIndex=0;festIndex<manifestFiles.size();festIndex++){
            def productPath=manifestFiles[festIndex].path.replaceAll(arProduct_Manifest,'')
            echo "product package path: "+productPath.replaceAll('/src','')
            def version_ARProduct_Package=getVersionOfARProductFromManifest(manifestFiles[festIndex].path)
            def files = findFiles(glob: productPath+'/*'+version_ARProduct_Package+'*')
            if(local_linux){
                sh( returnStatus: true, script: '''ssh '''+local_linux+'''  'mkdir -p '''+arproduct_repo_linux+productVersionFolder+'''' ''')
                for(int index=0;index<files.size();index++){
                    echo "transfer ${files[index].name} to folder $productVersionFolder"
                    def fileExisted=sh(returnStdout: true, script: '''ssh '''+local_linux+''' '[ -e '''+arproduct_repo_linux+productVersionFolder+'/'+files[index].name+''' ]; echo $?' ''').trim()
                    if(fileExisted=='0'){
                        echo "Agile Reporter Product Package already exists.No need to download again."
                    }else{
                        sh( returnStatus: true, script: 'scp '+files[index].path+' '+local_linux+':'+arproduct_repo_linux+productVersionFolder+'/'+files[index].name)
                    }
                }
            }
            withAWS(credentials: 'aws') {
                files.each{s3Upload( bucket:s3_bucket, path:"arproduct/${projectFolder}/CandidateReleases/${productVersionFolder}/${it.name}",includePathPattern:"${it.path}")}
            }
        }
    }else{
        error "there is no packages, generated with failures."
    }
}



void updateBuild(projectFolder, buildnumber){
    def productVersion=getProductVersionFolder(projectFolder)
    def packageSection = createPackagesSection(projectFolder, productVersion,buildnumber)
    currentBuild.displayName=productVersion+'-'+buildnumber
    currentBuild.description = """
		$packageSection
    """
}

def createPackagesSection(projectFolder, productVersion, buildNumber) {
    def propertiesSet=getProps(projectFolder)
    def bucketName=propertiesSet['s3.bucket']
    def s3repo='arproduct/'+projectFolder+'/CandidateReleases/'+productVersion
    def downloadlink
    def packageLinksRows=''
    def displaylink=''
    def downfiles
    withAWS(credentials: 'aws') {
        downfiles=s3FindFiles(bucket:bucketName, path:s3repo, glob:"**/*$buildNumber*")
    }
    downfiles.each{print "${it.name},${it.path},${it.length},${it.lastModified}"}
    for(int index=0;index<downfiles.size();index++){
        downloadlink=createLink(bucketName,s3repo,downfiles[index].path,downfiles[index].name)
        if(downfiles[index].name.toLowerCase().startsWith(projectFolder.toLowerCase())){
            displaylink=displaylink+"""<h4 style='margin: 3px 0'>$downloadlink</h4>&#x000A;&#x000D;"""
        }
        packageLinksRows=packageLinksRows+
                """<tr>
                <td>${downfiles[index].name}</td>
                <td>$downloadlink</td>
            </tr>"""
    }

    return displaylink+convertToTable(
            description: 'Packages',
            firstColumn: 'Package Name', secondColumn: 'Download Link',
            rows: packageLinksRows
    )
}

String createLink(bucketName,s3repo,downloadpath,downloadname){
    def rootUrl="https://s3-eu-west-1.amazonaws.com"
    return """<a href = '$rootUrl/$bucketName/$s3repo/${downloadpath}' title = 'Download [${downloadname}] from the [$bucketName] S3 bucket'>${downloadpath}</a>"""
}

def convertToTable(Map args) {
    return """
        <h3 style='margin-bottom:5px'>${args.description}:</h3>
        <table class='pane stripped-odd bigtable' style='text-align: left'>
            <thead>
                <tr>
                    <th class='pane-header' style='width:40%'>${args.firstColumn}</th>
                    <th class='pane-header' style='width:30%'>${args.secondColumn}</th>
                </tr>
            </thead>
            <tbody>
                ${args.rows}
            </tbody>
        </table>"""
}


def getFileNames(projectFolder){
    def arProduct_Manifest='/src/manifest.xml'
    def manifestFiles = findFiles(glob: '**/'+projectFolder+'/*/target/**'+arProduct_Manifest)
    def returnAllFiles=''
    if(manifestFiles){
        for(int festIndex=0;festIndex<manifestFiles.size();festIndex++){
            def productPath=manifestFiles[festIndex].path.replaceAll(arProduct_Manifest,'')
            echo "product package path: "+productPath.replaceAll('/src','')
            def version_APRroduct_Pacakge=getVersionOfARProductFromManifest(manifestFiles[festIndex].path)
            def files = findFiles(glob: productPath+'/*'+version_APRroduct_Pacakge+'*')

            files.each{returnAllFiles=returnAllFiles+it.name+':'}

            println returnAllFiles
        }
    }else{
        error "there is no packages, generated with failures."
    }
    returnAllFiles=returnAllFiles[0..returnAllFiles.length()-2]
    return returnAllFiles
}

def triggerOtherJob(projectFolder,packageBuildNumber){
    println "run another job for download to local......"
    def productVersionFolder=getProductVersionFolder(projectFolder)
    def S3_DOWNPATH='arproduct/'+projectFolder+'/CandidateReleases/'+productVersionFolder+'/'+packageBuildNumber+'/'
    def LOCAL_ARCHIVED_PATH=S3_DOWNPATH.replace('arproduct/','ARProduct/').replace('CandidateReleases/','candidate-release/')
    def props=getProps(projectFolder)
    def S3_BUCKET=props['s3.bucket']
    def DOWNLOADFILENAMES=getFileNames(projectFolder)
    jobB = build job: 'download_from_s3', parameters: [string(name: 'S3BUCKET', value: "$S3_BUCKET"), string(name: 'S3DOWNPATH', value: "$S3_DOWNPATH"), string(name: 'ARCHIVEDPATH', value: "$LOCAL_ARCHIVED_PATH"), string(name: 'DOWNLOADFILENAMES', value: "$DOWNLOADFILENAMES")]
    println jobB.getResult()
}

def triggerOtherJobWithJobName(projectFolder,packageBuildNumber,downloadJobName){
    println "run another job for download to local......"
    def productVersionFolder=getProductVersionFolder(projectFolder)
    def S3_DOWNPATH='arproduct/'+projectFolder+'/CandidateReleases/'+productVersionFolder+'/'+packageBuildNumber+'/'
    def LOCAL_ARCHIVED_PATH=S3_DOWNPATH.replace('arproduct/','ARProduct/').replace('CandidateReleases/','candidate-release/')
    def props=getProps(projectFolder)
    def S3_BUCKET=props['s3.bucket']
    def DOWNLOADFILENAMES=getFileNames(projectFolder)
    jobB = build job:downloadJobName , parameters: [string(name: 'S3BUCKET', value: "$S3_BUCKET"), string(name: 'S3DOWNPATH', value: "$S3_DOWNPATH"), string(name: 'ARCHIVEDPATH', value: "$LOCAL_ARCHIVED_PATH"), string(name: 'DOWNLOADFILENAMES', value: "$DOWNLOADFILENAMES")]
    println jobB.getResult()
}
