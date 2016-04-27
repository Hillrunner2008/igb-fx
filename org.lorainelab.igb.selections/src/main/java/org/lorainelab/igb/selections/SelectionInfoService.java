package org.lorainelab.igb.selections;

import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;

/**
 *
 * @author dcnorris
 */
public interface SelectionInfoService {

    ObjectProperty<Optional<GenomeVersion>> getSelectedGenomeVersion();

    ObjectProperty<Optional<Chromosome>> getSelectedChromosome();
    
    ObservableSet<CompositionGlyph> getSelectedGlyphs();
}
