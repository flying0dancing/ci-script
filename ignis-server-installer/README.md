## ignis-server-installer modules
- creates IzPack-based installer for installing `FCR Engine ignis-server`
- used for running FCR Engine against AWS EMR cluster, platform-tools cluster 
or a client's own cluster
- depends on artifacts from modules
  - ignis-db-migration
  - ignis-server
  - ignis-spark-staging
  - ignis-spark-validation

### ignis-server-izpack
- contains custom IzPack source code 
- validates the installer dir structure and install properties
- used by `ignis-server-izpack-installer` when building the installer jar

### ignis-server-izpack-installer
- contains scripts and config files used at start-up
- creates installer dir structure
- creates zip with libs required to run Apache Spark with Apache Phoenix persistence
- builds ignis-server installer jar and zip

## Running the installer
- `DEFAULT_INSTALL_PATH` is `~/fcr-engine/ignis-server/${project.version}`