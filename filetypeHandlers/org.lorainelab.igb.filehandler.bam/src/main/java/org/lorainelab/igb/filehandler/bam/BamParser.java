/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.filehandler.bam;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import htsjdk.samtools.seekablestream.SeekableBufferedStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.CloserUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.filehandler.api.DataType;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.data.model.glyph.Glyph;
import org.lorainelab.igb.data.model.shapes.Composition;
import org.lorainelab.igb.data.model.shapes.Line;
import org.lorainelab.igb.data.model.shapes.Rectangle;
import org.lorainelab.igb.data.model.shapes.Shape;
import org.lorainelab.igb.data.model.shapes.factory.GlyphFactory;
import org.lorainelab.igb.data.model.view.Layer;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
@Component(immediate = true)
public class BamParser implements FileTypeHandler {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BamParser.class);

    private SearchService searchService;
    private SelectionInfoService selectionInfoService;
        private ISeekableStreamFactory streamFactory;

    public BamParser() {
        streamFactory = SeekableStreamFactory.getInstance();
    }
        

    @Override
    public String getName() {
        return "Bam File";
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Sets.newHashSet("bam");
    }

    @Override
    public Set<CompositionGlyph> getRegion(String dataSourceReference, Range<Integer> range, Chromosome chromosome) {
        String chromosomeId = chromosome.getName();
        Set<BamFeature> annotations = Sets.newHashSet();
        selectionInfoService.getSelectedChromosome().get().ifPresent(selectedChromosome -> {
            selectedChromosome.loadRegion(range);
            String referenceSequence = new String(selectedChromosome.getSequence(range.lowerEndpoint(), range.upperEndpoint()));
            final String indexPath = dataSourceReference + ".bai";
            try (   
                    SeekableBufferedStream bamStream = new SeekableBufferedStream(streamFactory.getStreamFor(dataSourceReference));
                    SeekableBufferedStream indexStream = new SeekableBufferedStream(streamFactory.getStreamFor(indexPath));
                ) {

                SamReader reader = SamReaderFactory.make()
                        .validationStringency(ValidationStringency.SILENT)
                        .open(SamInputResource.of(bamStream).index(indexStream));

                final List<SAMSequenceRecord> seqRecords = reader.getFileHeader().getSequenceDictionary().getSequences();
                getSequenceByName(chromosomeId, seqRecords).ifPresent((SAMSequenceRecord record) -> {

                    QueryInterval[] intervals = new QueryInterval[]{
                        new QueryInterval(record.getSequenceIndex(), range.lowerEndpoint(), range.upperEndpoint())
                    };

                    try (SAMRecordIterator iter = reader.query(intervals, true)) {
                        while (iter.hasNext()) {
                            SAMRecord samRecord = iter.next();
                            int alignmentStart = samRecord.getAlignmentStart() - 1; // convert to interbase
                            int alignmentEnd = samRecord.getAlignmentEnd();
                            int width = alignmentEnd - alignmentStart;
                            alignmentStart -= range.lowerEndpoint();
                            annotations.add(new BamFeature(samRecord, referenceSequence.substring(alignmentStart, alignmentStart + width)));
                        }
                    }
                });

                CloserUtil.close(reader);
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }

        });
        return convertBamFeaturesToCompositionGlyphs(annotations);
    }

    private Optional<SAMSequenceRecord> getSequenceByName(String sequence, List<SAMSequenceRecord> seqRecords) {
        return seqRecords.stream().filter(r -> sequence.equals(r.getSequenceName())).findFirst();
    }

    private Set<CompositionGlyph> convertBamFeaturesToCompositionGlyphs(Set<BamFeature> annotations) {
        Set<CompositionGlyph> primaryGlyphs = Sets.newLinkedHashSet();
        String[] label = {""};
        Map[] tooltipData = {Maps.newConcurrentMap()};
        annotations.stream().map((BamFeature annotation) -> {
            BamRenderer view = new BamRenderer();
            final Composition composition = view.render(annotation);
            composition.getLabel().ifPresent(compositionLabel -> label[0] = compositionLabel);
            tooltipData[0] = composition.getTooltipData();
            return composition.getLayers();
        }).forEach(layersList -> {
            List<Glyph> children = Lists.newArrayList();
            layersList
                    .stream().forEach((Layer layer) -> {
                        getShapes(layer).forEach(shape -> {
                            if (Rectangle.class
                                    .isAssignableFrom(shape.getClass())) {
                                children.add(GlyphFactory.generateRectangleGlyph((Rectangle) shape));

                            }
                            if (Line.class
                                    .isAssignableFrom(shape.getClass())) {
                                children.add(GlyphFactory.generateLine((Line) shape));
                            }
                        });
                    });
            primaryGlyphs.add(GlyphFactory.generateCompositionGlyph(label[0], tooltipData[0], children));
        });
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

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Override
    public Set<DataType> getDataTypes() {
        return Sets.newHashSet(DataType.ANNOTATION, DataType.ALIGNMENT);
    }

}
