#!/usr/bin/env bash
#Run in detached mode
#docker run -it -d --publish-all=true matt/platform-tools

#Connect to container
#winpty docker exec -it <container_id> bash

docker build -t matt/platform-tools .
winpty docker run --name platform-tools \
  --hostname=platform.tools.docker \
  -m 8g \
  -p 8088:8088 \
  -p 8031:8031 \
  -p 8032:8032 \
  -p 8042:8042 \
  -p 8050:8050 \
  -p 8025:8025 \
  -p 19888:19888 \
  -p 18080:18080 \
  -p 50070:50070 \
  -p 50075:50075 \
  -p 50010:50010 \
  -p 50020:50020 \
  -p 8765:8765 \
  -p 2181:2181 \
  -p 9000:9000 \
  -p 16010:16010 \
  -it matt/platform-tools
