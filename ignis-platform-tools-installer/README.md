## ignis-platform-tools-installer modules
- creates IzPack-based installer for installing `FCR Engine platform-tools`
- used for setting up an on-premise cluster or standalone (single box) 
deployment for FCR Engine
- depends on distributions of 
  - Apache ZooKeeper
  - Apache Hadoop (Yarn, HDFS)
  - Apache Phoenix
  - Apache Spark
  
### ignis-platform-tools-izpack
- contains custom IzPack source code 
- validates the installer dir structure and install properties
- used by `ignis-platform-tools-izpack-installer` when building 
the installer jar

### ignis-platform-tools-izpack-installer
- contains scripts and config files used at start-up
- creates installer dir structure
- builds platform-tools installer jar and zip
  - the download and antrun plugins will setup the installer dir structure
  - then the izpack plugin will create the installer jar
    - requires the installer dir structure to be the same as installed dir (`INSTALL_PATH`)
    
## Running the installer
- `install.sh` 
  - used for installing on a single machine (standalone mode)
  - when no parameters are passed, default values will be used 
  for at install-time
- `cluster-install.sh`
  - used for installing on multiple machines (cluster mode) 
- `DEFAULT_INSTALL_PATH` is `~/fcr-engine/platform-tools/${project.version}`

## Running Platform tools as docker container
The following file details how to run Platform tools in a docker container [here](ignis-platform-tools-izpack-installer/docs/docker-platform-tools.md) 
