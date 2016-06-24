package org.lorainelab.igb.data.model;

import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.sequence.ReferenceSequenceProvider;

/**
 *
 * @author dcnorris
 */
public class GenomeVersion {

    private String name;
    private String speciesName;
    private String description;
    private final UUID uuid;
    private final ReferenceSequenceProvider referenceSequenceProvider;
    private ObjectProperty<Optional<Chromosome>> selectedChromosomeProperty;
    private ObservableSet<DataSet> loadedDataSets;

    public GenomeVersion(String name, String speciesName, ReferenceSequenceProvider referenceSequenceProvider) {
        this(name, speciesName, referenceSequenceProvider, null,null);
    }

    public GenomeVersion(String name, String speciesName, ReferenceSequenceProvider referenceSequenceProvider, UUID uuid) {
        this(name, speciesName, referenceSequenceProvider, null,uuid);
    }

    public GenomeVersion(String name, String speciesName, ReferenceSequenceProvider referenceSequenceProvider, String description, UUID uuid) {
        this.name = name;
        this.speciesName = speciesName;
        this.description = description;
        this.referenceSequenceProvider = referenceSequenceProvider;
        if(uuid == null){
            uuid = UUID.randomUUID();
        }
        this.uuid = uuid;
        selectedChromosomeProperty = new SimpleObjectProperty(Optional.empty());
        loadedDataSets = FXCollections.observableSet(Sets.newHashSet());
    }
    
    public GenomeVersion(String name, String speciesName, ReferenceSequenceProvider referenceSequenceProvider, String description) {
        this(name, speciesName, referenceSequenceProvider, null,null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.name);
        hash = 31 * hash + Objects.hashCode(this.speciesName);
        hash = 31 * hash + Objects.hashCode(this.description);
        hash = 31 * hash + Objects.hashCode(uuid);
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
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.speciesName, other.speciesName)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        return true;
    }

}
