#!/bin/bash

hhelp(){
    echo Usage: zipProduct [sourceFullPath] [zipFullPath] [propertiesFullName] [jenkinsVariable]
}

logger()
{
    logFile=$1
    msg=$2
    if [ ! -f "${logFile}" ];then
      touch "${logFile}"
    fi
    echo "${msg}" | tee -a "${logFile}"
}

DelFile()
{
    if [ -f "$1" ];then
        logger "$2" "[info] delete existed file:$1."
        rm -f "$1"
    fi
}

selfPath=`pwd $(dirname $0)`
selfName=`basename $0`
logFullName=${selfPath}/${selfName%.*}.log
if [ -f "${logFullName}" ];then
    rm -f "${logFullName}"
fi
if [ "$#" = "4" ];then
    if [ "$1" = "--help" ];then
	    hhelp
    else
        cd "$1"
        if [ "$?" = "0" ];then
            sourchFullPath=$1
            zipFullPath=$2
            properties=$3
            
            if [ -d "${sourchFullPath}/dpm" -a -d "${sourchFullPath}/transforms" -a -d "${sourchFullPath}/forms" -a -f "${sourchFullPath}/manifest.xml" ];then
                dpmVersion=`awk -F '=' /^gen.product.dpm.version[^.]/'{print $2}' "${properties}"`
                perl -i.bak -pwe "s/mappingVersion\=\".*\"/mappingVersion\=\"${dpmVersion}\"/g" manifest.xml 
                logger "${logFullName}" "[info] dpm mapping version is set to $dpmVersion in manifest.xml"
                perl -i.bak -pwe "s/\<implementationVersion\>(\d+\.\d+\.\d+).*\<\/implementationVersion\>/\<implementationVersion\>\$1-$4\<\/implementationVersion\>/g" manifest.xml 
                abcVersion=$(perl -ne "print qq/\$1\n/ if(/\<implementationVersion\>(.*)\<\/implementationVersion\>/);" manifest.xml) 
                logger "${logFullName}" "[info] implementationVersion is updated to ${abcVersion} in manifest.xml"
                abcPrefix=`awk -F '=' /^package.name.prefix[^.]/'{print $2}' "${properties}"`
                arVersion=`awk -F '=' /^ar.installer.version[^.]/'{print $2}' "${properties}"`
                abcName=${abcPrefix}v${abcVersion}_for_AR_${arVersion}
                zipFullName=${zipFullPath}/${abcName}.zip
                lrmFullName=${zipFullPath}/${abcName}.lrm
                DelFile "${zipFullName}" "${logFullName}"
                DelFile "${lrmFullName}" "${logFullName}"
                
                logger "${logFullName}" "[info] package zip file [${zipFullName}]..."
                zip -r "${zipFullName}" dpm/*.accdb transforms forms manifest.xml | tee -a "${logFullName}"
                if [ -f "${zipFullName}" ];then
                    logger "${logFullName}" "[info] zip file is packaged."
                    signJar=`awk -F '=' /^ocelot.config.sign.jar.linux[^.]/'{print $2}' "${properties}"`
                    if [ -f "${signJar}" ];then
                        logger "${logFullName}" "[info] package lrm file [${lrmFullName}]..."
                        java -jar "${signJar}" "${zipFullName}"
                        mv "${zipFullPath}/${abcName}_sign.lrm" "${abcName}.lrm"
                        if [ -f "${lrmFullName}" ];then
                            logger "${logFullName}" "[info] package is packaged successfully."
                            exit 0
                        else
                            logger "${logFullName}" "[error] cannot find packaged file [${lrmFullName}]"
                        fi
                    else
                        logger "${logFullName}" "[error] cannot find jar for sign [${signJar}]"
                    fi
                else
                    logger "${logFullName}" "[error] package failure."
                fi
            else
                logger "${logFullName}" "[error] check folders(dpm, transforms, forms) and manifest.xml are existed."
            fi
        else
            logger "${logFullName}" "[error] no such directory: $1"
        fi
    fi
else
    hhelp
fi
exit 1
