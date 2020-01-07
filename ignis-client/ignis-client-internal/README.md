# Ignis Client Internal
This module allows the Spark Jobs to communicate with the Ignis Server application.

## REST API Callbacks
In order for the metadata database to be updated once a spark job has been completed this internal REST API is used.

For example when Staging has completed a [`Dataset`](../../ignis-server/src/main/java/com/lombardrisk/ignis/server/service/dataset/Dataset.java) 
needs to be created as a "pointer" to the staged data, this is done via [`CreateDatasetCall`](src/main/java/com/lombardrisk/ignis/client/internal/CreateDatasetCall.java)