#!/bin/bash

if [ X$1 == X-h -o X$1 == X-help -o X$1 == Xhelp ];then
  echo "--------------------------------------------------------------------------------------------------------"
  echo "this script can installing installing product packages or config DID"
  echo "                               "
  echo "Parameter introduction-- "
  echo "  1 installfolder: like /home/test/PIPEAR4FED "
  echo "  2 packagename: like hkma*b8.zip "
  echo ""
  echo "  install product package:"
  echo "   Usage: installfolder appFileName"
  echo "   this.sh /home/test/PIPEAR4FED AR_FED_Package_v1_16_0_6.zip"
  echo ""
  echo "----------------------------------------------------------------------------------------------------------"
  exit
fi

installfolder=$1
arproductname=$2
type=1
find "${installfolder}" -type f -name ${arproductname} -exec sh RemoteInstall.sh "${installfolder}" 1 {} \;

