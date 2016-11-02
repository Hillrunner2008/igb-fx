package org.lorainelab.igb.filehandler.tabix;

import aQute.bnd.annotation.component.Component;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import java.util.Set;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.filehandler.api.IndexedFileHandler;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = IndexedFileHandler.class)
public class TabixIndexedFileHandler implements IndexedFileHandler {

    @Override
    public String getName() {
        return "Tabix";
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Sets.newHashSet("tbi");
    }

    @Override
    public Set<CompositionGlyph> getRegion(String dataSourceReference, Range<Integer> range, Chromosome chromosome) {

        return Sets.newHashSet();
    }

}
