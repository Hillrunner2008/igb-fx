package org.lorainelab.igb.data.model;

import javafx.collections.ObservableSet;

/**
 *
 * @author dcnorris
 */
public interface GenomeVersionRegistry {

    ObservableSet<GenomeVersion> getRegisteredGenomeVersions();
}
