package org.lorainelab.igb.data.model;

import aQute.bnd.annotation.component.Component;
import com.google.common.collect.Sets;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = {GenomeVersionRegistry.class, GenomeVersionSelectionManager.class})
public class GenomeVersionRegistryImpl implements GenomeVersionRegistry, GenomeVersionSelectionManager {

    private final ObservableSet<GenomeVersion> registeredGenomeVersions;
    private GenomeVersion selectedGenomeVersion;

    public GenomeVersionRegistryImpl() {
        registeredGenomeVersions = FXCollections.observableSet(Sets.newConcurrentHashSet());
    }

    @Override
    public ObservableSet<GenomeVersion> getRegisteredGenomeVersions() {
        return registeredGenomeVersions;
    }

    @Override
    public Optional<GenomeVersion> getSelectedGenomeVersion() {
        return Optional.ofNullable(selectedGenomeVersion);
    }

    @Override
    public void setSelectedGenomeVersion(GenomeVersion selectedGenomeVersion) {
        this.selectedGenomeVersion = selectedGenomeVersion;
    }

}
