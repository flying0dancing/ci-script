#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
DEFAULT_INSTALL_PATH="${HOME}/fcr-engine/"#APP_SUBPATH

usage() {
    cat << USAGE >&2
Usage:
    cluster-install.sh -p  [- w ] [-u ] [-o ] [-i ]
      -p  Required: absolute path of system.properties
      -w  Optional: remote working directory (defaults to current dir: ${DIR})
      -u  Optional: remote user (defaults to current user: ${USER})
      -o  Optional: ssh options (defaults to password-less ssh)
      -i  Optional: install path (defaults to path: ${DEFAULT_INSTALL_PATH})
USAGE
    exit 1
}


while [[ $# -gt 0 ]]
do
    case "$1" in
      -w)
      WORKING_PATH=$2
       if [[ "$WORKING_PATH" == "" ]]; then
         echo "Error: remote working path must not be empty"
         exit 1
      fi
      shift 2
      ;;
      -p)
      PROP_FILE="$2"
      if [[ "$PROP_FILE" == "" ]]; then
         echo "Error: system properties file must not be empty"
         exit 1
      fi
      if [[ ! -f  ${PROP_FILE} ]]; then
        PROP_FILE="${DIR}"/${PROP_FILE}
      fi
      if [[ ! -f  ${PROP_FILE} ]]; then
        echo  "Error: system properties file must exist at ${PROP_FILE}"
        exit 1
      fi
      shift 2
      ;;
      -u)
      REMOTE_USER=$2
      if [[ "$REMOTE_USER" == "" ]]; then
         echo "Error: remote user must not be empty"
         exit 1
      fi
      shift 2
      ;;
      -i)
      INSTALL_PATH=$2
      if [[ "$INSTALL_PATH" == "" ]]; then
         echo "Error: install path must not be empty"
         exit 1
      fi
      shift 2
      ;;
      -h)
      usage;
      ;;
      -o)
      SSH_OPTS=$2
       if [[ "$SSH_OPTS" == "" ]]; then
         echo "Error: SSH options must not be empty"
         exit 1
      fi
      shift 2
      ;;
      *)
      usage
      ;;
    esac
done

REMOTE_USER="${REMOTE_USER:-${USER}}"
WORKING_PATH="${WORKING_PATH:-${DIR}}"
SSH_OPTS="${SSH_OPTS:-""}"
INSTALL_PATH="${INSTALL_PATH:-${DEFAULT_INSTALL_PATH}}"

PROP_FILE_NAME="$(basename ${PROP_FILE})"

HOSTS=$(grep '^\s*hosts=' ${PROP_FILE} | tail -n 1 | cut -d "=" -f2-)
HOSTS="${HOSTS//,/ }"

if [[ "${#HOSTS}" -eq "0" ]]; then
    echo "'hosts' property must not be blank or missing"
    exit 1
fi

for host in `echo ${HOSTS}`; do
  echo "Synchronize local dir [${DIR}]"
  echo "        to remote dir [$host:${WORKING_PATH}]"

  ssh ${SSH_OPTS} ${REMOTE_USER}@${host}  bash -c "'
     if [ ! -d "${WORKING_PATH}" ]; then
       echo Create dir/s [${WORKING_PATH}]
       mkdir -p ${WORKING_PATH}
     fi
  '"  2>&1 | sed "s/^/${host}: /"

  rsync -avzopg --progress "${PROP_FILE}" ${REMOTE_USER}@${host}:${WORKING_PATH}/
  rsync -avzopg --progress "${DIR}" ${REMOTE_USER}@${host}:${WORKING_PATH}/..

  ssh ${SSH_OPTS} ${REMOTE_USER}@${host} bash -c "'
       cd "${WORKING_PATH}"
       chmod u+x install.sh
       chmod u+x cluster-install.sh
       ./install.sh -p ${DIR}/${PROP_FILE_NAME} -i ${INSTALL_PATH}
  '" 2>&1 | sed "s/^/${host}: /"
done