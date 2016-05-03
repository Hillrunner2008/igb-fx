package org.lorainelab.igb.data.model.sequence;

import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.Chromosome;

public interface ReferenceSequenceProvider {

    String getSequence(String chromosomeId);

    String getSequence(String chromosomeId, int start, int length);

    ObservableSet<Chromosome> getChromosomes();

}
