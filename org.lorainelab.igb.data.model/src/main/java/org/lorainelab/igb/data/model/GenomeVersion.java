package org.lorainelab.igb.data.model;

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
public class GenomeVersion {

    private String name;
    private String speciesName;
    private String description;
    private final ReferenceSequenceProvider referenceSequenceProvider;
    private ObjectProperty<Optional<Chromosome>> selectedChromosomeProperty;
    private ObservableSet<DataSet> loadedDataSets;

    public GenomeVersion(String name, String speciesName, ReferenceSequenceProvider referenceSequenceProvider) {
        this(name, speciesName, referenceSequenceProvider, null);
    }

    public GenomeVersion(String name, String speciesName, ReferenceSequenceProvider referenceSequenceProvider, String description) {
        this.name = name;
        this.speciesName = speciesName;
        this.description = description;
        this.referenceSequenceProvider = referenceSequenceProvider;
        selectedChromosomeProperty = new SimpleObjectProperty(Optional.empty());
        loadedDataSets = FXCollections.observableSet(Sets.newHashSet());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public ReferenceSequenceProvider getReferenceSequenceProvider() {
        return referenceSequenceProvider;
    }

    public ObjectProperty<Optional<Chromosome>> getSelectedChromosomeProperty() {
        return selectedChromosomeProperty;
    }

    public void setSelectedChromosome(Chromosome selectedChromosome) {
        selectedChromosomeProperty.set(Optional.ofNullable(selectedChromosome));
    }

    public ObservableSet<DataSet> getLoadedDataSets() {
        return loadedDataSets;
    }

}
