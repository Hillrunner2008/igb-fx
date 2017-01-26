package org.lorainelab.igb.selections;

import java.util.Optional;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;

/**
 *
 * @author dcnorris
 */
public interface SelectionInfoService {

    ReadOnlyObjectProperty<Optional<GenomeVersion>> getSelectedGenomeVersion();

    ReadOnlyObjectProperty<Optional<Chromosome>> getSelectedChromosome();

    ObservableSet<CompositionGlyph> getSelectedGlyphs();
}
