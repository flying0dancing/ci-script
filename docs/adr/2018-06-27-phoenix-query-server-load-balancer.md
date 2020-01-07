# Phoenix Query Server Load Balancer 
In clustered deployment a client may have many Query Servers for Apache Phoenix to balance the load on the servers.
These servers server up endpoints for JDBC requests which are made via HTTP by the 
`org.apache.phoenix.phoenix-queryserver-client`.

This client is known as a "thin client" because it just makes jdbc requests to server without much knowledge of the
hadoop infrastructure. 


## Status 
Accepted: Requires Testing

## Context
This feature was added when coding [ARCE-529](https://jira.lombardrisk.com/browse/ARCE-529) which requires `ignis-server` 
to query the Phoenix Database (previously there was no dependency between server and phoenix)

## Current Functionality
Previously, even when multiple query servers were configured AgileReporter (previously the only user of the phoenix client)
only one of the Query Server's urls was used by AR. Therefore all other query servers were not being used.

## Proposed Functionality
It was proposed that the client would configure their own load balancer for these query servers and use this load balancer url
in Agile Reporter.

It was also proposed that this load balancer url be provided in the installation UI as well as provided in the
installation properties.

The following property was added to `system.properties`
```
phoenix.qs.load.balancer=${phoenix.qs.load.balancer}
```

#### Load Balancer Configuration
The Load Balancer will have to be sticky as jdbc connections can often be pooled by clients and maintain a stateful 
connection.

It is therefore proposed that the Load Balancer be a sticky session load balancer where the state is managed by the load
balancer.


#### Actions for client installation
If using Cluster installation the client will need to set up a load balancer for the query servers and provide it in the
installation properties or through the installer UI.