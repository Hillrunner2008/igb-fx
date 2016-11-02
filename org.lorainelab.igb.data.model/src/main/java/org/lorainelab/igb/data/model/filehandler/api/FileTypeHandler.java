package org.lorainelab.igb.data.model.filehandler.api;

import com.google.common.collect.Range;
import java.util.Set;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;

/**
 *
 * @author dcnorris
 */
public interface FileTypeHandler {

    /**
     * The preferred name to be used to describe this format. This name will be used in
     * file extension filters as the name describing 1 or more extensions. (e.g. Bed File).
     *
     * @return preferred name
     */
    String getName();

    /**
     * @return the file extensions (there may be more than one) for this
     * file type
     */
    Set<String> getSupportedExtensions();

    /**
     * Get a region of the chromosome.
     *
     * @param dataSourceReference
     * @param range - start and stop of the requested region
     * @return Set of features in the requested range
     */
    Set<CompositionGlyph> getRegion(String dataSourceReference, final Range<Integer> range, Chromosome chromosome);

    /**
     * Get all features in a chromosome
     *
     * @return Set of features in the requested range
     */
    default Set<CompositionGlyph> getChromosome(String dataSourceReference, Chromosome chromosome) {
        return getRegion(dataSourceReference, Range.closed(0, chromosome.getLength()), chromosome);
    }

    Set<DataType> getDataTypes();

}
