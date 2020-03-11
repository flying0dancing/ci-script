

void upload(projectFolder,packageBuildNumber){
    def arProduct_Manifest='/src/manifest.xml'
    def productVersionFolder=productPackage.getProductVersionFolder(projectFolder)
    def props=productPackage.getProps(projectFolder)
    def s3_bucket=props['s3.bucket']
    def local_linux=props['local.linux']
    def arproduct_repo_linux=props['arproduct.repo.linux']+projectFolder+'/candidate-release/'
    def manifestFiles = findFiles(glob: '**/'+projectFolder+'/*/target/**'+arProduct_Manifest)
    productVersionFolder=productVersionFolder+'/'+packageBuildNumber
    if(manifestFiles){
        for(int festIndex=0;festIndex<manifestFiles.size();festIndex++){
            def productPath=manifestFiles[festIndex].path.replaceAll(arProduct_Manifest,'')
            echo "product package path: "+productPath.replaceAll('/src','')
            def version_ARProduct_Package=productPackage.getVersionOfARProductFromManifest(manifestFiles[festIndex].path)
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

void put2LocalRepoBySHLocal1(projectFolder,packageBuildNumber){
    def local_linux='test@172.20.31.7'
    def arproduct_repo_linux='repository/ARProduct/'+projectFolder+'/candidate-release/'
    def local_credentials='product-ci-sha-local1-user-test'
    put2LocalRepoInmost(projectFolder,packageBuildNumber,local_linux,arproduct_repo_linux,local_credentials)
}


void put2LocalRepo(projectFolder,packageBuildNumber){
    def props=productPackage.getProps(projectFolder)
    def local_linux=props['local.linux']
    def arproduct_repo_linux=props['arproduct.repo.linux']+projectFolder+'/candidate-release/'
    def local_credentials=props['local.linux.credentials']
    put2LocalRepoInmost(projectFolder,packageBuildNumber,local_linux,arproduct_repo_linux,local_credentials)
}

void put2LocalRepoInmost(projectFolder,packageBuildNumber,local_linux,arproduct_repo_linux,local_credentials){
    def arProduct_Manifest='/src/manifest.xml'
    def productVersionFolder=productPackage.getProductVersionFolder(projectFolder)
    def installerNames=''
    def manifestFiles = findFiles(glob: '**/'+projectFolder+'/*/target/**'+arProduct_Manifest)
    productVersionFolder=productVersionFolder+'/'+packageBuildNumber
    def installerParent=arproduct_repo_linux+productVersionFolder
    if(local_linux){
        sshagent(credentials: [local_credentials]){
            if(manifestFiles){
                sh( returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $local_linux  'mkdir -p ${installerParent}' ")
                for(int festIndex=0;festIndex<manifestFiles.size();festIndex++){
                    def productPath=manifestFiles[festIndex].path.replaceAll(arProduct_Manifest,'')
                    echo "product package path: "+productPath.replaceAll('/src','')
                    def version_ARProduct_Package=productPackage.getVersionOfARProductFromManifest(manifestFiles[festIndex].path)
                    def files = findFiles(glob: productPath+'/*'+version_ARProduct_Package+'*')
                    for(int index=0;index<files.size();index++){
                        installerNames=installerNames+files[index].name+':'
                        echo "transfer ${files[index].name} to folder $productVersionFolder"
                        def installerFullName=installerParent+'/'+files[index].name
                        def fileExisted=sh(returnStatus: true, script: "ssh -o StrictHostKeyChecking=no $local_linux '[ -e \"$installerFullName\" ]' ")
                        if(fileExisted==0){
                            echo "Agile Reporter Product Package already exists.No need to download again."
                        }else{
                            sh( returnStatus: true, script: 'scp -o StrictHostKeyChecking=no '+files[index].path+' '+local_linux+':'+installerFullName)
                        }
                    }
                }
                installerNames=installerNames[0..installerNames.length()-2]
                println installerNames
            }else{
                error "there is no packages, generated with failures."
            }
        }
    }

}



