package org.lorainelab.igb.data.model;

import java.util.Set;

public interface ReferenceSequenceProvider {

    String getSequence(String chromosomeId);
    String getSequence(String chromosomeId, int start, int length);

    Set<Chromosome> getChromosomes();

}
