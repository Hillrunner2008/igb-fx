package org.lorainelab.igb.data.model;

import java.util.Optional;

/**
 *
 * @author dcnorris
 */
public class GenomeVersion {

    private String name;
    private String speciesName;
    private String description;
    private final ReferenceSequenceProvider referenceSequenceProvider;

    public GenomeVersion(String name, String speciesName, ReferenceSequenceProvider referenceSequenceProvider) {
        this(name, speciesName, referenceSequenceProvider, null);
    }

    public GenomeVersion(String name, String speciesName, ReferenceSequenceProvider referenceSequenceProvider, String description) {
        this.name = name;
        this.speciesName = speciesName;
        this.description = description;
        this.referenceSequenceProvider = referenceSequenceProvider;
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

}
