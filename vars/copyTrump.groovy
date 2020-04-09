/**
 * copy trump and update test.json and test.properties from deploy folder
 * @param propertiesSet
 * @return
 */
def call(workspace, projectFolder, deployFolder, destFolder="c:\\ar_auto\\trump"){
    def src="$workspace\\trump"
    def files="*.*"
    echo "copy trump"
    bat(returnStatus: true, script: "@robocopy $src $destFolder $files /E /NP 1>nul")
    //src="$workspace\\trump\\public"
    //dest="$destFolder"
    //files="*.*"
    //echo "copy public to $destFolder"
    //flag=bat(returnStatus: true, script: "robocopy $src $dest $files /E /NP 1>nul")
    src="$workspace\\$projectFolder\\src\\main\\resources\\$deployFolder"
    dest="$destFolder\\trump-sel\\src\\test\\resources"
    files="test.json test.properties"
    echo "update test.json and test.properties in trump-sel"
    bat(returnStatus: true, script: "@robocopy $src $destFolder $files /NP")

    //copy scripts like RunTest.bat getNewFullName.bat
    src="$workspace\\scripts"
    files="RunTest.bat"
    echo "copy RunTest.bat in trump"
    bat(returnStatus: true, script: "@robocopy $src $destFolder $files /NP")

}