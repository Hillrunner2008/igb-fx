package org.lorainelab.igb.selections;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class SelectionInfoServiceImpl implements SelectionInfoService {

    private GenomeVersionRegistry genomeVersionRegistry;
    private final ObjectProperty<Optional<Chromosome>> selectedChromosomeProperty;

    public SelectionInfoServiceImpl() {
        selectedChromosomeProperty = new SimpleObjectProperty<>(Optional.empty());
    }

    @Activate
    public void activate() {
        genomeVersionRegistry.getSelectedGenomeVersion().addListener((obs, oldValue, newValue) -> {
            newValue.ifPresent(genomeVersion -> {
                selectedChromosomeProperty.bind(genomeVersion.getSelectedChromosomeProperty());
            });
        });
    }

    @Override
    public ObjectProperty<Optional<GenomeVersion>> getSelectedGenomeVersion() {
        return genomeVersionRegistry.getSelectedGenomeVersion();
    }

    @Override
    public ObjectProperty<Optional<Chromosome>> getSelectedChromosome() {
        return selectedChromosomeProperty;
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }
}
