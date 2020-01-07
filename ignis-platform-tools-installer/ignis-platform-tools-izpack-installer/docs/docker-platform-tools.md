# Running Platform Tools with Docker

## Requirements
Docker for windows

## Building and Running
The following script `./run-platform-tools-docker.sh` will build and run the docker image.

### Before running

#### Platform tools installer
Before running this script you will need to build the platform tools installer for the docker container to use.

#### Spark Libs without Hadoop
In order to run against a real YARN cluster we need to build the `__spark_libs__.zip` without hadoop 
(as hadoop is provided by YARN), in order to do this we need to run the maven build for `ignis-server-izpack-installer` 
using the following maven profile `archive`. I.e. the following
```
$ mvn clean install -pl ignis-server-installer/ignis-server-izpack-installer -P archive
``` 

## Setting host file (Windows)
The hostname for platform tools docker container is set to `platform.tools.docker` therefore for the docker container
to be used correctly by local IntelliJ we need to configure the host `platform.tools.docker` to point towards localhost.

Open the following file `C:\Windows\System32\drivers\etc\hosts` and add the following line

```
127.0.0.1 platform.tools.docker
```

## Set IP_ADDRESS path variable
To run 'Ignis Server Docker' first add a new path variable called IP_ADDRESS with your laptop IP in 
Settings > Appearance & Behaviour > Path Variables

## Running out of disk space
Building and run docker images can lead to disk space running out for docker containers. This can be a problem for HDFS 
and YARN. To reduce disk space filling up run this command
```bash
$ docker system df //lists free disk space
$ docker system prune
```
