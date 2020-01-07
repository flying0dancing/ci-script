package com.lombardrisk.ignis.platform.tools.izpack.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Service {

    private final Node node;
    private final Component component;
    private final String id;

    Service(Node node, Component component) {
        id = UUID.randomUUID().toString();
        this.node = node;
        this.component = component;

        if (component.isMaster()) {
            this.node.addMaster(this);
        }
        if (component.isSlave()) {
            this.node.addSlave(this);
        }
        if (component.isClient()) {
            this.node.addClient(this);
        }
    }
}
