/**
 * copy trump and update test.json and test.properties from deploy folder
 * @param propertiesSet
 * @return
 */
def call(workspace, projectFolder, deployFolder, destParentDir="c:\\ar_auto"){
    def src="$workspace\\trump\\trump-sel"
    def dest="$destParentDir\\trump-sel"
    def files="*.*"
    echo "copy trump-sel to $destParentDir"
    def flag=bat(returnStatus: true, script: "robocopy $src $dest $files /E /NP 1>nul")
    echo "$flag"
    if(flag==0){
        //src="$workspace\\trump\\public"
        //dest="$destParentDir"
        //files="*.*"
        //echo "copy public to $destParentDir"
        //flag=bat(returnStatus: true, script: "robocopy $src $dest $files /E /NP 1>nul")
        src="$workspace\\$projectFolder\\src\\main\\resources\\$deployFolder"
        dest="$destParentDir\\trump-sel\\src\\test\\resources"
        files="test.json test.properties"
        echo "update test.json and test.properties in trump-sel"
        flag=bat(returnStatus: true, script: "robocopy $src $dest $files /E /NP")

        //copy scripts like RunTest.bat getNewFullName.bat
        src="$workspace\\scripts"
        dest="$destParentDir\\trump-sel"
        files="RunTest.bat"
        echo "update test.json and test.properties in trump-sel"
        flag=bat(returnStatus: true, script: "robocopy $src $dest $files /E /NP")
        dest="%USERPROFILE%"
        files="getNewFullName.bat"
        bat(returnStatus: true, script: "robocopy $src $dest $files /E /NP")
    }
}