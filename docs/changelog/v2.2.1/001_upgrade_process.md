# Upgrading platform tools
#### Status - Done
FCR Engine platform tools dependencies have been upgraded to the following versions:

| Dependency | before 2.2.0-b718 | 2.2.0-b718 onwards | 2.2.1  |
|------------|-------------------|--------------------|--------|
| Hadoop     | 2.7.3             | 2.7.3              | 2.8.3  |
| HBase      | 1.3.1             | 1.3.1              | 1.4.0  |
| Phoenix    | 4.11.0            | 4.13.0             | 4.13.0 |
| Spark      | 2.2.1             | 2.2.1              | 2.2.1  |
| Zookeeper  | 3.4.10            | 3.4.10             | 3.4.10 |

## Steps for clients to take
The script platform-tools/2.2.1/bin/upgrade.sh has been included to migrate from Hadoop 2.7.3 to 2.8.3. This involves
performing a rolling upgrade of Hadoop as documented [here](https://hadoop.apache.org/docs/r2.8.3/hadoop-project-dist/hadoop-hdfs/HdfsRollingUpgrade.html).

When upgrading platform-tools to version 2.2.1 from versions prior this script should be run. This script only needs to 
be run once. If installing 2.2.1 as a fresh installation (i.e. not upgrading from a previous version) then this script does
 not need to be run at all.
 
This script must be run with two arguments:

```bash
Usage: upgrade.sh (upgrade|finalize|rollback) PREVIOUS_PLATFORM_TOOLS_DIR
```

- **upgrade** - start Hadoop upgrade. This will create an image of HDFS in the case a rollback is required, shutdown services run 
by previous version of platform-tools and start upgraded versions of those services. At this point the client can proceed to 
test FCR-Engine and AR against the upgraded services.
- **finalize** - finalize the Hadoop upgrade. This will delete the HDFS image created from the upgrade step. At this point the 
upgrade is complete and it is **impossible to rollback to the previous version of Hadoop**.
- **rollback** - rollback to the previous version of Hadoop. In the case the upgrade is not successful this step can be run to 
rollback and cancel the upgrade. **Any data saved between running upgrade and rollback will be LOST**.
- **PREVIOUS_PLATFORM_TOOLS_DIR** - required. The directory of the previous version of platform tools. For example if the previous 
version was installed at /home/myuser/fcr-engine/2.2.0-bx/ and the new version is installed at /home/myuser/fcr-engine/2.2.1-bx/ then
this value should be **/home/myuser/fcr-engine/2.2.0-bx**

Follow these steps to perform the platform-tools upgrade. For this example the previous version of platform tools is installed
at **/home/myuser/fcr-engine/platform-tools/2.2.0-b719** and the new version of platform-tools will be installed into **/home/myuser/fcr-engine/platform-tools/2.2.1**.

1. Unzip and install the new version of platform-tools as normal using either install.sh or cluster-install.sh
2. In FCR make sure there are no jobs running and stop ignis-server application. Do NOT stop the previous version of platform-tools.
3. Make sure the previous version of platform-tools is running by running /home/myuser/fcr-engine/platform-tools/2.2.0-b719/bin/status.sh.
All services should be showing as up. If not investigate before proceeding with the upgrade.
4. Go to the new platform-tools directory at /home/myuser/fcr-engine/platform-tools/2.2.1 and run the following
command ```./bin/upgrade.sh upgrade /home/myuser/fcr-engine/platform-tools/2.2.0-b719```
5. Confirm the new version of platform-tools is running:
    1. Running status.sh show all services as up
    2. Hadoop name node and data nodes versions are showing as 2.8.3 at http://platform-tools-host:50070
    3. HBase version is showing as 1.4.0 at http://platform-tools-host:16010
6. Begin testing FCR and AR
    1. Test existing returns in AR still work and show correct data
    2. Query existing data from Phoenix using either sqlline.py or DBeaver
    3. Stage new data in FCR and retrieve from AR should still work
    4. If testing is successful finalize the upgrade by proceeding to step 7. Otherwise cancel the upgrade by proceeding to step 8
7. Finalize the upgrade by running ```./bin/upgrade.sh finalize /home/myuser/fcr-engine/platform-tools/2.2.0-b719```
8. Rollback the upgrade by running ```./bin/upgrade.sh rollback /home/myuser/fcr-engine/platform-tools/2.2.0-b719```
9. OPTIONAL: Install fcr-engine-platform-tools as a service using ```./bin service.sh install```
