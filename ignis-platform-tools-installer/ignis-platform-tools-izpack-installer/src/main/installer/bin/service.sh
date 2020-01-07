#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

usage() {
   echo "Setup FCR Engine platform-tools as a simple systemd service"
   echo "Usage: service.sh (install|uninstall|start|stop|status)"
}

install() {
    checkSystemdInitSystem

    systemctl disable fcr-engine-platform-tools
    cp "${DIR}/ext/fcr-engine-platform-tools.service" /etc/systemd/system/
    systemctl enable fcr-engine-platform-tools

    reloadSystemdUnits
}

uninstall() {
  checkSystemdInitSystem

  systemctl disable fcr-engine-platform-tools
  rm /etc/systemd/system/fcr-engine-platform-tools.service || true

  reloadSystemdUnits
}

reloadSystemdUnits() {
  systemctl daemon-reload
}

start() {
  checkSystemdInitSystem

  systemctl start fcr-engine-platform-tools
}

stop() {
  checkSystemdInitSystem

  systemctl stop fcr-engine-platform-tools
}

status() {
  checkSystemdInitSystem

  systemctl status fcr-engine-platform-tools
}

checkSystemdInitSystem() {
  systemdRunDir="/run/systemd/system"

  if [[ -d "${systemdRunDir}" && ! -L "${systemdRunDir}" ]]; then
    :
  else
    echo "Cannot setup FCR Engine platform-tools as a service"
    echo "systemd is not the init systemd currently in use"
    exit 1
  fi
}

commandToRun=$1

case "${commandToRun}" in
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

