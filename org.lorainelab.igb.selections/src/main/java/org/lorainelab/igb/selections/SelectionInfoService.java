package org.lorainelab.igb.selections;

import java.util.Optional;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;

/**
 *
 * @author dcnorris
 */
public interface SelectionInfoService {

    Optional<GenomeVersion> getSelectedGenomeVersion();

    Optional<Chromosome> getSelectedChromosome();
}
