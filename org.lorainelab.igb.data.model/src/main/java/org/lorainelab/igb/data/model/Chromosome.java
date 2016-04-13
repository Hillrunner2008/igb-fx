package org.lorainelab.igb.data.model;

/**
 *
 * @author dcnorris
 */
public class Chromosome {

    private String name;
    private int length;
    private final ReferenceSequenceProvider referenceSequenceProvider;

    public Chromosome(String name, int length, ReferenceSequenceProvider referenceSequenceProvider) {
        this.name = name;
        this.length = length;
        this.referenceSequenceProvider = referenceSequenceProvider;
    }

    public void setLastName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public ReferenceSequenceProvider getReferenceSequenceProvider() {
        return referenceSequenceProvider;
    }

}
