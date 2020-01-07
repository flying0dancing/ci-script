#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
DEFAULT_INSTALL_PATH="${HOME}/fcr-engine/"#APP_SUBPATH

if type -p java; then
    echo "Using java executable from \$PATH"
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "Using java executable from \$JAVA_HOME"
    _java="$JAVA_HOME/bin/java"
else
    echo "Could not find a java executable!"
    exit 1
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "$version" < "1.8" ]]; then
        echo "java executable version can not be less than 1.8"
        exit 1
    fi
    echo "$("$_java" -version)"
fi

usage() {
    cat << USAGE >&2
Usage:
    install.sh [-p <file>] [-i <dir>] [-gui | -console]
      -p    defaults file (auto/unattended mode)
      -i    install path (defaults to path: ${DEFAULT_INSTALL_PATH})
      -gui  use GUI mode
      -con  use console mode (default mode)
      -h    show usage
USAGE
    exit 1
}

consoleModeFlag="-console"
unattendedModeFlag="-auto"
defaultFileArg=""

while [[ $# -gt 0 ]]
do
    case "$1" in
      -p)
      defaultsFile="$2"
      if [[ "$defaultsFile" == "" ]]; then
        echo "-p option requires a defaults file path"
         usage
      elif [[ ! -f "${DIR}/${defaultsFile}" ]] && [[ -f "$(pwd)/${defaultsFile}" ]]; then
         defaultsFile="$(pwd)/${defaultsFile}"
      fi
      defaultFileArg="-defaults-file $defaultsFile"
      unattendedModeFlag="-auto"
      shift 2
      ;;
      -i)
      INSTALL_PATH="$2"
       if [[ "$INSTALL_PATH" == "" ]]; then
         echo "-i option requires a installation path"
         usage
      fi
      shift 2
      ;;
      -gui)
      unattendedModeFlag=""
      consoleModeFlag=""
      shift 1
      ;;
      -con)
      unattendedModeFlag=""
      consoleModeFlag="-console"
      shift 1
      ;;
      -h)
      usage;
      ;;
      *)
      usage
      ;;
    esac
done

INSTALL_PATH="${INSTALL_PATH:-${DEFAULT_INSTALL_PATH}}"
mkdir -p "${INSTALL_PATH}/logs"

INSTALL_PATH="$(cd "$(dirname "$INSTALL_PATH")"; pwd)/$(basename "$INSTALL_PATH")"
installerJar=$(find "${DIR}"/*installer*.jar  -type f -printf "%f\n")
installerJar="${DIR}/${installerJar}"



${_java} -DINSTALLER_PATH=${DIR} \
    -DINSTALL_PATH=${INSTALL_PATH} \
    -jar ${installerJar} \
    -stacktrace \
    ${defaultFileArg} \
    ${consoleModeFlag} \
    ${unattendedModeFlag}