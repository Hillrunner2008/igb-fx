package org.lorainelab.igb.data.model;

import aQute.bnd.annotation.component.Component;
import com.google.common.collect.Sets;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class GenomeVersionRegistryImpl implements GenomeVersionRegistry {

    private final ObservableSet<GenomeVersion> registeredGenomeVersions;
    private final ObjectProperty<Optional<GenomeVersion>> selectedGenomeVersionProperty;

    public GenomeVersionRegistryImpl() {
        registeredGenomeVersions = FXCollections.observableSet(Sets.newConcurrentHashSet());
        selectedGenomeVersionProperty = new SimpleObjectProperty<>(Optional.empty());
    }

    @Override
    public ObservableSet<GenomeVersion> getRegisteredGenomeVersions() {
        return registeredGenomeVersions;
    }

    @Override
    public void setSelectedGenomeVersion(GenomeVersion selectedGenomeVersion) {
        selectedGenomeVersionProperty.setValue(Optional.ofNullable(selectedGenomeVersion));
    }

    @Override
    public ObjectProperty<Optional<GenomeVersion>> getSelectedGenomeVersion() {
        return selectedGenomeVersionProperty;
    }

}
