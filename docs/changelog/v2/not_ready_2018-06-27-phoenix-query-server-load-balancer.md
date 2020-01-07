# Phoenix Query Server Load Balancer 
## Status - Not Ready
In clustered deployment a client may have many Query Servers for Apache Phoenix to balance the load on the servers.

A property has been added to the install properties to allow the FCR server to use a client configured load balancer.

## Steps for clients to take
Add the following property to the install properties file
```
phoenix.qs.load.balancer=${phoenix.qs.load.balancer}
```

## Query Server Host
Currently in the install property set up we define the following property.
```
phoenix.qs.hosts=host1,host2,host3,...hostn
phoenix.qs.http.port=8765
```

The load balancer should allow access through the same port that the query servers listen on (above it is `8765`)

### Opting out
If the client chooses not to install a load balancer the load balancer property should be one of the hosts of the 
Phoenix Query servers.

I.e.
```
phoenix.qs.hosts=host1,host2,host3,...hostn
phoenix.qs.load.balancer=host1
```
