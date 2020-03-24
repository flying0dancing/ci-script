#!/bin/bash


if [ X$1 == X-h -o X$1 == X-help -o X$1 == Xhelp ];then
  echo "--------------------------------------------------------------------------------------------------------"
  echo "this script can get agile reporter url"
  echo "                               "
  echo "Parameter introduction-- "
  echo "  1 propertiesFileFullName: like /home/test/PIPEAR4FED/ocelot.properties "
  echo ""
  echo "   Usage: propertiesFileFullName"
  echo "   this.sh /home/test/PIPEAR4FED/ocelot.properties"
  echo ""
  echo " Notes: if app or properties are already in installfolder, only type their FileName;"
  echo "        if installfolder does not exist, this.sh will create a new one."
  echo "        product's propertiesFileName must under bin folder"
  echo "----------------------------------------------------------------------------------------------------------"
  exit
fi

properties=$1
http_head="http://"
port=None
if [ -f "${properties}" ];then
  #host_ip=$(awk -F '=' /^main.host.name[^.]/'{print $2}' "${properties}")
  host_ip=$(ifconfig | grep 'inet addr:'|grep -v 127.0.0.1 | awk -F'[: ]+' '{print $4}')
  port_base=`awk -F '=' /^host.port[^.]/'{print $2}' "${properties}"`
  port_offset=`awk -F '=' /^host.port.offset/'{print $2}' "${properties}"`
  https_mode=`awk -F '=' /^httpsMode/'{print $2}' "${properties}"`
  if [ -n "${port_offset}" ];then
      let port=${port_base}+${port_offset}
  else
      port=${port_base}
  fi
  if [ "${https_mode}" = "true" ];then
    http_head="https://"
  fi
  echo "$http_head$host_ip:$port"
else
  exit 1
fi