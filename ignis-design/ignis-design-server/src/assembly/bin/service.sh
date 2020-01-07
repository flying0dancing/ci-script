#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

DESIGN_STUDIO_HOME="$(dirname $DIR)"
DESIGN_STUDIO_BIN="${DIR}"
USER_NAME="$(logname)"

usage() {
   echo "Setup FCR Engine design-studio as a simple systemd service"
   echo "Usage: service.sh (install|uninstall|start|stop|status)"
}

install() {
    checkSystemdInitSystem

    systemctl disable design-studio

    sed 's|${USER_NAME}|'${USER_NAME}'|g' "${DESIGN_STUDIO_BIN}/"design-studio.service.template \
    | sed 's|${DESIGN_STUDIO_HOME}|'${DESIGN_STUDIO_HOME}'|g' \
    | sed 's|${DESIGN_STUDIO_BIN}|'${DESIGN_STUDIO_BIN}'|g' > /etc/systemd/system/design-studio.service

    systemctl enable design-studio

    reloadSystemdUnits
}

uninstall() {
  checkSystemdInitSystem

  systemctl disable design-studio
  rm /etc/systemd/system/design-studio.service || true

  reloadSystemdUnits
}

reloadSystemdUnits() {
  systemctl daemon-reload
}

start() {
  checkSystemdInitSystem

  systemctl start design-studio
}

stop() {
  checkSystemdInitSystem

  systemctl stop design-studio
}

status() {
  checkSystemdInitSystem

  systemctl status design-studio
}

checkSystemdInitSystem() {
  systemdRunDir="/run/systemd/system"

  if [[ -d "${systemdRunDir}" && ! -L "${systemdRunDir}" ]]; then
    :
  else
    echo "Cannot setup Design Studio as a service"
    echo "systemd is not the init systemd currently in use"
    exit 1
  fi
}

commandToRun=$1

case "$commandToRun" in
  install)
     install
     ;;
  uninstall)
     uninstall
     ;;
  status)
     status
     ;;
 start)
     start
     ;;
 stop)
     stop
     ;;
  *)
     usage;
     ;;
esac
