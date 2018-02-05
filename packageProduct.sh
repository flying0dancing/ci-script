#!/bin/bash

selfPath=`dirname $(readlink -f "$0")`
sourchFullPath=${selfPath}/../$1
zipFullPath=${selfPath}/../$1
properties=${selfPath}/../$2
echo ${sourchFullPath} ${zipFullPath} ${properties} $3
sh ${selfPath}/zipProduct.sh ${sourchFullPath} ${zipFullPath} ${properties} $3
