package com.lombardrisk.ignis.platform.tools.izpack.core;

import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.lombardrisk.ignis.platform.tools.izpack.core.Node.LOCALHOST;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

@Getter
public class Cluster {

    private final SortedSet<Node> nodes;
    private final Set<Service> services;

    public Cluster(final String... hosts) {
        nodes = Arrays.stream(hosts)
                .map(Node::new)
                .collect(toCollection(TreeSet::new));
        services = createServices();
    }

    private Set<Service> createServices() {
        Set<Service> componentServices = new HashSet<>();
        RoundRobinAllocation nodeAllocation = new RoundRobinAllocation(nodes);

        componentServices.addAll(
                Component.getMasters()
                        .stream()
                        .map(master -> new Service(nodeAllocation.allocate(), master))
                        .collect(toList()));

        componentServices.addAll(
                Component.getSlaves()
                        .stream()
                        .flatMap(slave -> nodes.stream()
                                .map(node -> new Service(node, slave)))
                        .collect(toList()));

        componentServices.addAll(
                Component.getClients()
                        .stream()
                        .map(client -> new Service(nodeAllocation.allocate(), client))
                        .collect(toList()));
        return componentServices;
    }

    public String getHost(final Component component) {
        Node node =
                services.stream()
                        .filter(service -> service.getComponent().equals(component))
                        .findFirst()
                        .map(Service::getNode)
                        .orElse(LOCALHOST);
        return node.getHost();
    }

    public List<String> getHosts(final Component component) {
        return services.stream()
                .filter(service -> service.getComponent().equals(component))
                .map(service -> service.getNode().getHost())
                .distinct()
                .collect(toList());
    }

    static class RoundRobinAllocation {

        private Deque<Node> queue;
        private final Collection<Node> nodes;

        RoundRobinAllocation(final Collection<Node> nodes) {
            queue = new ArrayDeque<>(nodes);
            this.nodes = nodes;
        }

        Node allocate() {
            if (queue.isEmpty()) {
                queue = new ArrayDeque<>(nodes);
            }
            return queue.pop();
        }
    }
}