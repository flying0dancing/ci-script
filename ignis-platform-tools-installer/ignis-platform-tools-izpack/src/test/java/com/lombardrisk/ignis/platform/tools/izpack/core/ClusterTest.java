package com.lombardrisk.ignis.platform.tools.izpack.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.DATANODE;
import static com.lombardrisk.ignis.platform.tools.izpack.core.Component.NAMENODE;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ClusterTest {

    @Test
    public void createFromHosts() {
        Cluster cluster = new Cluster("node1", "node2", "node3");

        assertThat(cluster.getNodes().size())
                .isEqualTo(3);
    }

    @Test
    public void createFromInstallData() {
        Cluster cluster = new Cluster("node1", "node2", "node3");

        assertThat(cluster.getNodes().size())
                .isEqualTo(3);
        assertThat(cluster.getHost(NAMENODE))
                .isEqualTo("node1");
    }

    @Test
    public void getHost() {
        Cluster cluster = new Cluster("node1", "node2", "node3");

        assertThat(cluster.getHost(NAMENODE))
                .isEqualTo("node1");
    }

    @Test
    public void getHosts() {
        Cluster cluster = new Cluster("node1", "node2", "node3");

        assertThat(cluster.getHosts(DATANODE).size())
                .isEqualTo(3);
    }

    @Test
    public void getNodes() {
        Cluster cluster = new Cluster("node1", "node2", "node3");

        assertThat(cluster.getNodes().size())
                .isEqualTo(3);
    }

    @Test
    public void getServices() {
        Cluster cluster = new Cluster("node1", "node2", "node3");

        int mastersCount = 6;
        int nodesCount = 3;
        int slavesCount = 4;
        int clientsCount = 2;

        assertThat(cluster.getServices().size())
                .isEqualTo(mastersCount + nodesCount * slavesCount + clientsCount);
    }
}