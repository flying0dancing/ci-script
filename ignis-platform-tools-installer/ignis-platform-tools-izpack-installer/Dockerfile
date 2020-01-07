FROM openjdk:8

#Install ssh and other utilities
RUN mkdir fcr-installer && \
      apt-get update && apt-get install -y \
      openssh-server \
      vim

ADD target/installer /fcr-installer
COPY src/test/resources/system.properties /fcr-installer/system.properties
COPY src/test/resources/docker-start.sh /fcr-installer/docker-start.sh
COPY src/test/resources/core-site.xml /fcr-installer/core-site.xml

CMD [ "sh", "-c", "./fcr-installer/docker-start.sh; bash"]
