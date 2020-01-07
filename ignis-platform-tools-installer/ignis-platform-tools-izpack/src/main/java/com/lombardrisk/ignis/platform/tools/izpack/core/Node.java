package com.lombardrisk.ignis.platform.tools.izpack.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(of = "host")
@ToString
public class Node implements Comparable<Node> {

    static final Node LOCALHOST = new Node("localhost");

    @Getter
    private final String host;

    private final Set<Service> masters = new HashSet<>();
    private final Set<Service> slaves = new HashSet<>();
    private final Set<Service> clients = new HashSet<>();

    Node(String host) {
        this.host = host;
    }

    void addMaster(Service service) {
        masters.add(service);
    }

    void addSlave(Service service) {
        slaves.add(service);
    }

    void addClient(Service service) {
        clients.add(service);
    }

    @Override
    public int compareTo(final Node other) {
        return host.compareTo(other.getHost());
    }
}
