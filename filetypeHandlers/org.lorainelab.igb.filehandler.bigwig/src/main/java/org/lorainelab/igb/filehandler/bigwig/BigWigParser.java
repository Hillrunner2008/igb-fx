package org.lorainelab.igb.filehandler.bigwig;

import aQute.bnd.annotation.component.Component;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeRangeMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import org.broad.igv.bbfile.BBFileHeader;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.WigItem;
import org.lorainelab.igb.data.model.datasource.DataSource;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.data.model.glyph.GraphGlyph;
import org.lorainelab.igb.search.api.model.IndexIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class BigWigParser implements FileTypeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BigWigParser.class);

    @Override
    public String getName() {
        return "BigWig File";
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Sets.newHashSet("bw");
    }

    @Override
    public Set<CompositionGlyph> getRegion(DataSourceReference dataSourceReference, Range<Integer> range, String chromosomeId) {
        final DataSource dataSource = dataSourceReference.getDataSource();
        final String path = dataSourceReference.getPath();
        Set<BigWigFeature> features = Sets.newLinkedHashSet();
        try {
            BBFileReader bbReader = new BBFileReader(path);
            BBFileHeader bbFileHdr = bbReader.getBBFileHeader();
            if (bbFileHdr.isBigWig()) {
                BigWigIterator bigWigIterator = bbReader.getBigWigIterator(chromosomeId, range.lowerEndpoint(), chromosomeId, range.upperEndpoint(), true);
                try {
                    WigItem wigItem = null;
                    while (bigWigIterator.hasNext()) {
                        wigItem = bigWigIterator.next();
                        if (wigItem == null) {
                            break;
                        }
                        features.add(new BigWigFeature(chromosomeId, wigItem));
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return convertBigWigFeaturesToCompositionGlyphs(features);
    }

    private Set<CompositionGlyph> convertBigWigFeaturesToCompositionGlyphs(Set<BigWigFeature> features) {
        Set<CompositionGlyph> primaryGlyphs = Sets.newLinkedHashSet();

        RangeMap<Double, Double> intervals = TreeRangeMap.create();

        for (BigWigFeature f : features) {
            final double lowerEndpoint = f.getRange().lowerEndpoint();
            final double upperEndpoint = f.getRange().upperEndpoint() - 1;
            intervals.put(Range.<Double>closed(lowerEndpoint, upperEndpoint), f.getyValue());
        }
        final HashMap<String, String> toolTip = new HashMap<String, String>();
        toolTip.put("forward", Boolean.TRUE.toString());
        primaryGlyphs.add(new CompositionGlyph("Graph", toolTip, Lists.newArrayList(new GraphGlyph(intervals))));
        return primaryGlyphs;
    }

    @Override
    public Set<CompositionGlyph> getChromosome(DataSourceReference dataSourceReference, String chromosomeId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getSearchIndexKeys() {
        return Sets.newHashSet();
    }

    @Override
    public void createIndex(IndexIdentity indexIdentity, DataSourceReference dataSourceReference) {
        //do nothing
    }

    @Override
    public boolean isGraphType() {
        return true;
    }

}
