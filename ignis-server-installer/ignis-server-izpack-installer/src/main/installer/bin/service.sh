#!/usr/bin/env bash

BIN_PATH="%{INSTALL_PATH}/bin"

usage() {
   echo "Setup FCR Engine ignis-server as a simple systemd service"
   echo "Usage: service.sh (install|uninstall|start|stop|status)"
}

install() {
    checkSystemdInitSystem

    systemctl disable fcr-engine-ignis-server
    cp "${BIN_PATH}/fcr-engine-ignis-server.service" /etc/systemd/system/
    systemctl enable fcr-engine-ignis-server

    reloadSystemdUnits
}

uninstall() {
  checkSystemdInitSystem

  systemctl disable fcr-engine-ignis-server
  rm /etc/systemd/system/fcr-engine-ignis-server.service || true

  reloadSystemdUnits
}

reloadSystemdUnits() {
  systemctl daemon-reload
}

start() {
  checkSystemdInitSystem

  systemctl start fcr-engine-ignis-server
}

stop() {
  checkSystemdInitSystem

  systemctl stop fcr-engine-ignis-server
}

status() {
  checkSystemdInitSystem

  systemctl status fcr-engine-ignis-server
}

checkSystemdInitSystem() {
  systemdRunDir="/run/systemd/system"

  if [[ -d "${systemdRunDir}" && ! -L "${systemdRunDir}" ]]; then
    :
  else
    echo "Cannot setup FCR Engine ignis-server as a service"
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