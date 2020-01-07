package com.lombardrisk.ignis.platform.tools.izpack.core;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Getter
public enum Component {
    NAMENODE(true, false, false),
    SECONDARY_NAMENODE(true, false, false),
    HISTORY_SERVER(true, false, false),
    RESOURCE_MANAGER(true, false, false),
    HMASTER(true, false, false),
    ZOOKEEPER(true, false, false),

    DATANODE(false, true, false),
    NODE_MANAGER(false, true, false),
    REGION_SERVER(false, true, false),
    QUERY_SERVER(false, true, false),
    SPARK_HISTORY_SERVER(false, false, true),
    IGNIS(false, false, true);

    private final boolean master;
    private final boolean slave;
    private final boolean client;

    Component(final boolean master, final boolean slave, final boolean client) {
        this.master = master;
        this.slave = slave;
        this.client = client;
    }

    public static List<Component> getMasters() {
        return Arrays.stream(Component.values())
                .filter(Component::isMaster)
                .collect(toList());
    }

    public static Set<Component> getSlaves() {
        return Arrays.stream(Component.values())
                .filter(Component::isSlave)
                .collect(toSet());
    }

    public static Set<Component> getClients() {
        return Arrays.stream(Component.values())
                .filter(Component::isClient)
                .collect(toSet());
    }
}
