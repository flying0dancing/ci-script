#!/bin/bash

/etc/init.d/ssh start

fcr-installer/install.sh -p fcr-installer/system.properties -i /root/platform-tools
cp fcr-installer/core-site.xml root/platform-tools/hadoop-2.8.3/etc/hadoop/core-site.xml

ssh-keygen -b 2048 -t rsa -f /root/.ssh/id_rsa -q
cat /root/.ssh/id_rsa.pub >> /root/.ssh/authorized_keys
chmod +x /root/.ssh/authorized_keys

USE_HOSTNAME_PROPERTY="<property>\n<name>dfs.client.use.datanode.hostname</name>\n<value>true</value>\n</property>"
sed -i "/<\/configuration>/i ${USE_HOSTNAME_PROPERTY}" root/platform-tools/hadoop-2.8.3/etc/hadoop/hdfs-site.xml

yes | root/platform-tools/bin/start.sh
