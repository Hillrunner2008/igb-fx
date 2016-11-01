package org.lorainelab.igb.filehandler.bigwig;

import aQute.bnd.annotation.component.Component;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import org.broad.igv.bbfile.BBFileHeader;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.WigItem;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.chart.IntervalChart;
import org.lorainelab.igb.data.model.datasource.DataSource;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.lorainelab.igb.data.model.filehandler.api.DataType;
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
        return Sets.newHashSet("bw", "bw.gz");
    }

    @Override
    public Set<CompositionGlyph> getRegion(DataSourceReference dataSourceReference, Range<Integer> range, Chromosome chromosome) {
        String chrId = chromosome.getName();
        final DataSource dataSource = dataSourceReference.getDataSource();
        final String path = dataSourceReference.getPath();
        IntArrayList x = new IntArrayList();
        IntArrayList w = new IntArrayList();
        DoubleArrayList y = new DoubleArrayList();
        try {
            BBFileReader bbReader = new BBFileReader(path);
            BBFileHeader bbFileHdr = bbReader.getBBFileHeader();
            if (bbFileHdr.isBigWig()) {
                BigWigIterator bigWigIterator = bbReader.getBigWigIterator(chrId, range.lowerEndpoint(), chrId, range.upperEndpoint(), true);
                try {
                    WigItem wigItem = null;
                    while (bigWigIterator.hasNext()) {
                        wigItem = bigWigIterator.next();
                        if (wigItem == null) {
                            break;
                        }
                        final int minX = wigItem.getStartBase();
                        final int maxX = wigItem.getEndBase();
                        final float wigValue = wigItem.getWigValue();
                        x.add(minX);
                        w.add(maxX - minX);
                        y.add(wigValue);
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        int dataSize = x.size();
        int[] xData = Arrays.copyOf(x.elements(), dataSize);
        int[] wData = Arrays.copyOf(w.elements(), dataSize);
        double[] yData = Arrays.copyOf(y.elements(), dataSize);
        return convertBigWigFeaturesToCompositionGlyphs(new IntervalChart(xData, wData, yData), chromosome);
    }

    private Set<CompositionGlyph> convertBigWigFeaturesToCompositionGlyphs(IntervalChart cd, Chromosome chromosome) {
        Set<CompositionGlyph> primaryGlyphs = Sets.newLinkedHashSet();
        final HashMap<String, String> toolTip = new HashMap<String, String>();
        toolTip.put("forward", Boolean.TRUE.toString());
        primaryGlyphs.add(new CompositionGlyph("Graph", toolTip, Lists.newArrayList(new GraphGlyph(cd, chromosome))));
        return primaryGlyphs;
    }

    @Override
    public Set<CompositionGlyph> getChromosome(DataSourceReference dataSourceReference, Chromosome chromosome) {
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
    public Set<DataType> getDataTypes() {
        return Sets.newHashSet(DataType.GRAPH);
    }

}
