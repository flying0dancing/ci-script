#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

createRequiredDirs() {
  mkdir -p "%{server.tomcat.basedir}"
  mkdir -p "%{dataset.source.location.localPath}"
  mkdir -p "%{tmp.path}"
}

makeExecutableScripts() {
  searchDir=$1
  echo "Set executable permissions for all .sh files under ${searchDir}"

  find "${searchDir}" -iname "*.sh" -or -iname 'flyway' | xargs chmod u+x
}

makeUserPrivateConfig() {
  echo "Set user private permissions for sensitive config files"

  find "${DIR}" -iname "*.properties" \
                -or -iname "*.keystore" \
                -or -iname "*.conf" \
  | xargs chmod a-rwx,u+rw
}

postInstall() {
  createRequiredDirs

  makeExecutableScripts "${DIR}"

  makeUserPrivateConfig
}

postInstall >> "${DIR}/logs/install.log"