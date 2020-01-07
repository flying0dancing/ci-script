# Split Installers
Split out the current `ignis-installer` into 2 installers:
- ignis-server installer 
- platform-tools installer


## Status 
Accepted


## Context
We currently build a single installer that installs FCR Engine by setting up a 
Apache Hadoop cluster, Apache Phoenix and our ignis-server.

Most of the time, we will update ignis-server code, because that is where our
business logic lives and we won't be updating the Apache technologies versions
or scripts.

This installer contains a couple of folders and 2 scripts for standalone 
installation and cluster installation. These get archived and stored in AWS S3,
as well as a network shared folder.


## Decision
Split the archived installer into: 
- `fcr-engine-ignis-server-installer` archive
    - contains only the dependencies required to setup `ignis-server` 
- `fcr-engine-platform-tools-installer` archive
    - contains the Apache technologies required to run our Spark jobs/apps

The two installers should be as independent as possible. 
Both installers should come with SystemD services and status scripts.
 

## Consequences
- faster builds as the Hadoop installer should change rarely
- easier support for running FCR Engine on AWS EMR more easily
- easier support for running FCR Engine for clients managing their own 
Hadoop cluster
- more complex build pipeline required in order to test full releases and
ignis releases