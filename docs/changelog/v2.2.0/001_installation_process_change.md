# Changes to Installation Process 
#### Status - Done
FCR now supports the option of installation against a pre-existing Hadoop cluster (with the same components and versions).
In order to support this the installation process has been split into two parts,
- Ignis Server installation
- Platform tools installation

The Ignis Server installation contains the Ignis server component, housing the metadata service and ui.
The Platform tools contain all of the Hadoop architecture to house client data.

## Steps for clients to take
Update property files to conform to new standard (see [example](001_example_system.properties)), DATA_PATH will need to be 
updated to the location of the old installations data folder. Then
- Stop ignis services in normal way
- Uninstall the fcr-engine service (if installed) using `./ignis/bin/service.sh uninstall`
- Run Platform Tools installation using `install.sh` or `cluster-install.sh` 
- Start up platform services only to verify data is still present after upgrade (see section below)
- Run Ignis Service installation
- Start up Ignis Service
- Verify that AgileReporter can drill down to datasets created before upgrade (using Allocations UI)

## Verify data is picked up by new installation
When upgrading to the latest version of FCR and installing Platform tools it will be necessary to verify that the
new Platform tools installation has picked up the data from the existing cluster. There are different ways to check this

### Phoenix server
FCR stores all its data on the Apache Phoenix Database. The following command can be used to verify that data has been 
picked up by the new Platform Tools installation.
```bash
[ec2-user@i-0bfa16f6c76e43cde ~]$ ./fcr-engine/platform-tools/1.0.0-SNAPSHOT/apache-phoenix-4.11.0-HBase-1.3-bin/bin/sqlline.py
Setting property: [incremental, false]
Setting property: [isolation, TRANSACTION_READ_COMMITTED]
issuing: !connect jdbc:phoenix:localhost:2181:/hbase none none org.apache.phoenix.jdbc.PhoenixDriver
Connecting to jdbc:phoenix:localhost:2181:/hbase
19/03/05 12:27:41 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Connected to: Phoenix (version 4.11)
Driver: PhoenixEmbeddedDriver (version 4.11)
Autocommit status: true
Transaction isolation: TRANSACTION_READ_COMMITTED
Building list of tables and columns for tab-completion (set fastconnect to true to skip)...
128/128 (100%) Done
Done
sqlline version 1.2.0
0: jdbc:phoenix:localhost:2181:/hbase> !tables
+------------+--------------+----------------------------------+---------------+
| TABLE_CAT  | TABLE_SCHEM  |            TABLE_NAME            |  TABLE_TYPE   |
+------------+--------------+----------------------------------+---------------+
|            | SYSTEM       | CATALOG                          | SYSTEM TABLE  |
|            | SYSTEM       | FUNCTION                         | SYSTEM TABLE  |
|            | SYSTEM       | SEQUENCE                         | SYSTEM TABLE  |
|            | SYSTEM       | STATS                            | SYSTEM TABLE  |
|            |              | FINANCIAL_PERFORMANCE            | TABLE         |
|            |              | NYSE_TRADES_DATA_SCHEDULES       | TABLE         |
|            |              | VALIDATION_RULE_RESULTS          | TABLE         |
+------------+--------------+----------------------------------+---------------+
0: jdbc:phoenix:localhost:2181:/hbase> !sql SELECT * FROM FINANCIAL_PERFORMANCE;
```

### Hdfs Cmd line tools
The Hadoop File System comes with a number of tools to navigate through the Hadoop file system.
Running the following command will list the contents of the Hadoop user's home directory on the file system
```bash
[ec2-user@i-0bfa16f6c76e43cde ~]$ ./fcr-engine/platform-tools/1.0.0-SNAPSHOT/hadoop-2.7.3/bin/hdfs dfs -ls
19/03/05 12:36:30 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Found 1 items
drwxr-xr-x   - ec2-user hadoop          0 2018-11-20 15:16 datasets
[ec2-user@i-0bfa16f6c76e43cde ~]$ ./fcr-engine/platform-tools/1.0.0-SNAPSHOT/hadoop-2.7.3/bin/hdfs dfs -ls datasets
19/03/05 12:36:35 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Found 8 items
drwxr-xr-x   - ec2-user hadoop          0 2018-11-20 14:00 datasets/10022
drwxr-xr-x   - ec2-user hadoop          0 2018-11-20 14:16 datasets/10024
drwxr-xr-x   - ec2-user hadoop          0 2018-11-20 14:19 datasets/10026
drwxr-xr-x   - ec2-user hadoop          0 2018-11-20 14:45 datasets/10028
drwxr-xr-x   - ec2-user hadoop          0 2018-11-20 15:02 datasets/10032
drwxr-xr-x   - ec2-user hadoop          0 2018-11-20 15:16 datasets/10036
drwxr-xr-x   - ec2-user hadoop          0 2018-10-23 10:24 datasets/10397
drwxr-xr-x   - ec2-user hadoop          0 2018-10-23 16:11 datasets/10493
[ec2-user@i-0bfa16f6c76e43cde ~]$
```

## Cleaning up
Now that the new Platform Tools have been installed all of the old services from the previous installation can be removed.
I.e. all folders under `ignis/services/*`

#### Be Careful when doing this - Do not delete the data directory!