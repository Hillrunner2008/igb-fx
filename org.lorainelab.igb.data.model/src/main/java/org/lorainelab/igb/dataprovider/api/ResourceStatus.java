package org.lorainelab.igb.dataprovider.api;

import java.util.Arrays;
import java.util.Optional;

/**
 *
 * @author dcnorris
 */
public enum ResourceStatus {

    NotInitialized("Not initialized"),
    Initialized("Initialized"),
    NotResponding("Not responding"),
    Disabled("Disabled");

    private final String name;

    ResourceStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<ResourceStatus> fromName(String name) {
        return Arrays.asList(values()).stream().filter(status -> status.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public String toString() {
        return name;
    }

}
