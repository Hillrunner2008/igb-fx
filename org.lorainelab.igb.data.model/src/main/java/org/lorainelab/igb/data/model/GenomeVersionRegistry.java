package org.lorainelab.igb.data.model;

import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;

/**
 *
 * @author dcnorris
 */
public interface GenomeVersionRegistry {

    ObservableSet<GenomeVersion> getRegisteredGenomeVersions();
    ObjectProperty<Optional<GenomeVersion>> getSelectedGenomeVersion();
    void setSelectedGenomeVersion(GenomeVersion selectedGenomeVersion);
}
