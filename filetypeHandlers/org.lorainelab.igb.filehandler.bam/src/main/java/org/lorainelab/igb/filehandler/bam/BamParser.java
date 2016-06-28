/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.filehandler.bam;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import htsjdk.samtools.QueryInterval;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.seekablestream.SeekableBufferedStream;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.util.CloserUtil;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.lorainelab.igb.data.model.datasource.DataSource;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.data.model.shapes.Shape;
import org.lorainelab.igb.data.model.view.Layer;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.search.api.model.IndexIdentity;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
@Component(immediate = true)
public class BamParser implements FileTypeHandler {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BamParser.class);

    private SearchService searchService;

    @Override
    public String getName() {
        return "Bam File";
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Sets.newHashSet("bed");
    }

    @Override
    public Set<CompositionGlyph> getRegion(DataSourceReference dataSourceReference, Range<Integer> range, String chromosomeId) {
        String path = dataSourceReference.getPath();
        Set<BamFeature> annotations = Sets.newHashSet();
        DataSource dataSource = dataSourceReference.getDataSource();
        try (SeekableBufferedStream bamSeekableStream = new SeekableBufferedStream(
                new SeekableFileStream(new File(dataSourceReference.getPath())));
                SeekableBufferedStream indexSeekableStream = new SeekableBufferedStream(
                        new SeekableFileStream(new File(dataSourceReference.getPath() + ".bai")));) {

            SamReader reader = SamReaderFactory.make()
                    .validationStringency(ValidationStringency.SILENT)
                    .open(SamInputResource.of(bamSeekableStream).index(indexSeekableStream));

            final List<SAMSequenceRecord> seqRecords = reader.getFileHeader().getSequenceDictionary().getSequences();
            getSequenceByName(chromosomeId, seqRecords).ifPresent((SAMSequenceRecord record) -> {

                QueryInterval[] intervals = new QueryInterval[]{
                    new QueryInterval(record.getSequenceIndex(), range.lowerEndpoint(), range.upperEndpoint())
                };
                
                try (SAMRecordIterator iter = reader.query(intervals, true)) {
                    while (iter.hasNext()) {
                        SAMRecord samRecord = iter.next();
                        annotations.add(new BamFeature(samRecord));
                    }
                }
            });

            CloserUtil.close(reader);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return convertBamFeaturesToCompositionGlyphs(annotations);
    }

    @Override
    public Set<CompositionGlyph> getChromosome(DataSourceReference dataSourceReference, String chromosomeId) {
        String path = dataSourceReference.getPath();
        Set<BamFeature> annotations = Sets.newHashSet();
        DataSource dataSource = dataSourceReference.getDataSource();
        try (SeekableBufferedStream bamSeekableStream = new SeekableBufferedStream(
                new SeekableFileStream(new File(dataSourceReference.getPath())));
                SeekableBufferedStream indexSeekableStream = new SeekableBufferedStream(
                        new SeekableFileStream(new File(dataSourceReference.getPath() + ".bai")));) {

            SamReader reader = SamReaderFactory.make()
                    .validationStringency(ValidationStringency.SILENT)
                    .open(SamInputResource.of(bamSeekableStream).index(indexSeekableStream));

            final List<SAMSequenceRecord> seqRecords = reader.getFileHeader().getSequenceDictionary().getSequences();
            getSequenceByName(chromosomeId, seqRecords).ifPresent((SAMSequenceRecord record) -> {

                int start = record.getSequenceIndex();
                int end = start + record.getSequenceLength();

                try (SAMRecordIterator iter = reader.query(record.getSequenceName(), start, end, true)) {
                    while (iter.hasNext()) {
                        SAMRecord samRecord = iter.next();
                        annotations.add(new BamFeature(samRecord));
                    }
                }
            });

            CloserUtil.close(reader);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return convertBamFeaturesToCompositionGlyphs(annotations);
    }

    private Optional<SAMSequenceRecord> getSequenceByName(String sequence, List<SAMSequenceRecord> seqRecords) {
        return seqRecords.stream().filter(r -> sequence.equals(r.getSequenceName())).findFirst();
    }

    private Set<CompositionGlyph> convertBamFeaturesToCompositionGlyphs(Set<BamFeature> annotations) {
        Set<CompositionGlyph> primaryGlyphs = Sets.newLinkedHashSet();
//        String[] label = {""};
//        Map[] tooltipData = {Maps.newConcurrentMap()};
//        annotations.stream().map((BamFeature annotation) -> {
//            BamRenderer view = new BamRenderer();
//            final Composition composition = view.render(annotation);
//            composition.getLabel().ifPresent(compositionLabel -> label[0] = compositionLabel);
//            tooltipData[0] = composition.getTooltipData();
//            return composition.getLayers();
//        }).forEach(layersList -> {
//            List<Glyph> children = Lists.newArrayList();
//            layersList
//                    .stream().forEach((Layer layer) -> {
//                        getShapes(layer).forEach(shape -> {
//                            if (Rectangle.class
//                            .isAssignableFrom(shape.getClass())) {
//                                children.add(GlyphFactory.generateRectangleGlyph((Rectangle) shape));
//
//                            }
//                            if (Line.class
//                            .isAssignableFrom(shape.getClass())) {
//                                children.add(GlyphFactory.generateLine((Line) shape));
//                            }
//                        });
//                    });
//            primaryGlyphs.add(GlyphFactory.generateCompositionGlyph(label[0], tooltipData[0], children));
//        });
        return primaryGlyphs;
    }

    private List<Shape> getShapes(Layer layer) {
        List<Shape> toReturn = Lists.newArrayList();
        layer.getItems().forEach(s -> {
            if (s instanceof Layer) {
                toReturn.addAll(getShapes((Layer) s));
            } else {
                toReturn.add(s);
            }
        });
        return toReturn;
    }

    @Override
    public Set<String> getSearchIndexKeys() {
        return Sets.newHashSet("id");
    }

    @Override
    public void createIndex(IndexIdentity indexIdentity, DataSourceReference dataSourceReference) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

}
