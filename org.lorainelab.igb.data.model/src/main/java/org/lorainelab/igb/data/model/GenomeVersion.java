package org.lorainelab.igb.data.model;

import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.sequence.ReferenceSequenceProvider;
import org.lorainelab.igb.dataprovider.model.DataContainer;

/**
 *
 * @author dcnorris
 */
public class GenomeVersion {

    private StringProperty name;
    private StringProperty speciesName;
    private StringProperty description;
    private ReferenceSequenceProvider referenceSequenceProvider;
    private ObjectProperty<Optional<Chromosome>> selectedChromosomeProperty;
    private ObservableSet<DataSet> loadedDataSets;
    private ObservableSet<DataContainer> dataContainers;
    private boolean custom;

    public GenomeVersion(String name, String speciesName, ReferenceSequenceProvider referenceSequenceProvider, boolean custom) {
        this(name, speciesName, referenceSequenceProvider, null, custom);
    }

    public GenomeVersion(String name, String speciesName, ReferenceSequenceProvider referenceSequenceProvider, String description, boolean custom) {
        this.name = new SimpleStringProperty(name);
        this.speciesName = new SimpleStringProperty(speciesName);
        this.description = new SimpleStringProperty(description);
        this.referenceSequenceProvider = referenceSequenceProvider;
        this.custom = custom;
        selectedChromosomeProperty = new SimpleObjectProperty(Optional.empty());
        loadedDataSets = FXCollections.observableSet(Sets.newHashSet());
        dataContainers = FXCollections.observableSet(Sets.newHashSet());
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public StringProperty name() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName.set(speciesName);
    }

    public Optional<StringProperty> getDescription() {
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

    public ObservableSet<DataContainer> getDataContainers() {
        return dataContainers;
    }

    public ObservableSet<DataSet> getLoadedDataSets() {
        return loadedDataSets;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.referenceSequenceProvider.getPath());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GenomeVersion other = (GenomeVersion) obj;
        if (!Objects.equals(this.referenceSequenceProvider.getPath(), other.referenceSequenceProvider.getPath())) {
            return false;
        }
        return true;
    }

}
