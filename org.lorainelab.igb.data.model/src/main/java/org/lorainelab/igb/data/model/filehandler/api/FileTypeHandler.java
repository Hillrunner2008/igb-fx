package org.lorainelab.igb.data.model.filehandler.api;

import com.google.common.collect.Range;
import java.util.Set;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;

/**
 *
 * @author dcnorris
 */
public interface FileTypeHandler {

    /**
     * The preferred name to be used to describe this format. This name will be used in
     * file extension filters as the name describing 1 or more extensions. (e.g. Bed File).
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
     * @param chromosomeId - the id of the chromosome
     * @return Set of features in the requested range
     */
    Set<CompositionGlyph> getRegion(DataSourceReference dataSourceReference, final Range range, String chromosomeId);

    /**
     * Get all features in a chromosome
     *
     * @param dataSourceReference
     * @param chromosomeId - the id of the chromosome
     * @return Set of features in the requested range
     */
    Set<CompositionGlyph> getChromosome(DataSourceReference dataSourceReference, String chromosomeId);
}
