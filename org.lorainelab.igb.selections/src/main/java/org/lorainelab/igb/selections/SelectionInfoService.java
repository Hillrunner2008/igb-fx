package org.lorainelab.igb.selections;

import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;

/**
 *
 * @author dcnorris
 */
public interface SelectionInfoService {

    ObjectProperty<Optional<GenomeVersion>> getSelectedGenomeVersion();

    ObjectProperty<Optional<Chromosome>> getSelectedChromosome();
}
