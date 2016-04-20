package org.lorainelab.igb.data.model;

import javafx.collections.ObservableSet;

public interface ReferenceSequenceProvider {

    String getSequence(String chromosomeId);
    String getSequence(String chromosomeId, int start, int length);

    ObservableSet<Chromosome> getChromosomes();

}
