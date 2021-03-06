package org.lorainelab.igb.filehandler.bed;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import htsjdk.tribble.readers.LineIteratorImpl;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.Strand;
import org.lorainelab.igb.data.model.filehandler.api.DataType;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.data.model.glyph.Glyph;
import org.lorainelab.igb.data.model.shapes.Composition;
import org.lorainelab.igb.data.model.shapes.Line;
import org.lorainelab.igb.data.model.shapes.Rectangle;
import org.lorainelab.igb.data.model.shapes.Shape;
import org.lorainelab.igb.data.model.shapes.factory.GlyphFactory;
import static org.lorainelab.igb.data.model.util.TabixUtils.getLineIterator;
import org.lorainelab.igb.data.model.view.Layer;
import org.lorainelab.igb.data.model.view.Renderer;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.synonymservice.ChromosomeSynomymService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class BedParser implements FileTypeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(BedParser.class);
    private Renderer<BedFeature> renderer;
    private SearchService searchService;
    private ChromosomeSynomymService chromosomeSynomymService;

    public BedParser() {
        renderer = new BedRenderer();//TODO make it possible to swap this for alternative renderers
    }

    private BedFeature createAnnotation(List<String> fields) {
        String chrom = fields.get(0);
        int annotationStart = Integer.parseInt(fields.get(1)); // start field
        int annotationEnd = Integer.parseInt(fields.get(2)); // stop field
        String name = null;
        boolean isForwardStrand;
        if (fields.size() >= 4) {
            name = fields.get(3);
            if (name.equals(".")) {
                //set to empty String
                name = "";
            }
        }
        float score = Float.NEGATIVE_INFINITY;
        if (fields.size() >= 5) {
            score = parseScore(fields.get(4));
        }
        if (fields.size() >= 6) {
            if (fields.get(5).equals(".")) {
                isForwardStrand = (annotationStart <= annotationEnd);
            } else {
                isForwardStrand = fields.get(5).equals("+");
            }
        } else {
            isForwardStrand = (annotationStart <= annotationEnd);
        }
        int min = Math.min(annotationStart, annotationEnd);
        int max = Math.max(annotationStart, annotationEnd);
        int thickStart = -1;
        int thickEnd = -1;
        if (fields.size() >= 8) {
            thickStart = Integer.parseInt(fields.get(6));
            thickEnd = Integer.parseInt(fields.get(7));
        }
        String itemRgb;
        if (fields.size() >= 9) {
            itemRgb = fields.get(8);
        }
        int exonCount = -1;
        int[] exonSizes = null;
        int[] exonStartPositions = null;
        if (fields.size() >= 12) {
            exonCount = Integer.parseInt(fields.get(9));
            exonSizes = parseIntArray(fields.get(10));
            exonStartPositions = parseIntArray(fields.get(11));
        }
        String description = "";
        if (fields.size() >= 14) {
            description = fields.get(13);
        }
        Range<Integer> annotationRange;
        if (annotationStart > annotationEnd) {
            annotationRange = Range.closedOpen(annotationEnd, annotationStart);
        } else {
            annotationRange = Range.closedOpen(annotationStart, annotationEnd);
        }
        BedFeature bedFeature = new BedFeature(chrom, annotationRange, isForwardStrand ? Strand.POSITIVE : Strand.NEGATIVE);
        bedFeature.setId(name);
        bedFeature.setCdsStart(thickStart);
        bedFeature.setCdsEnd(thickEnd);
        bedFeature.setLabel(name);
        bedFeature.setScore(score + "");
        bedFeature.setDescription(description);
        for (int i = 0; i < exonCount; i++) {
            final int exonStart = exonStartPositions[i];
            final int exonEnd = exonStartPositions[i] + exonSizes[i];
            bedFeature.getExons().add(Range.closedOpen(exonStart, exonEnd));
        }
        return bedFeature;
    }

    private int[] parseIntArray(String intArray) {
        if (Strings.isNullOrEmpty(intArray)) {
            return new int[0];
        }
        List<String> intstrings = Splitter.on(",").omitEmptyStrings().splitToList(intArray);
        int count = intstrings.size();
        int[] results = new int[count];
        for (int i = 0; i < count; i++) {
            int val = Integer.parseInt(intstrings.get(i));
            results[i] = val;
        }
        return results;
    }

    public boolean checkRange(int start, int end, int min, int max) {
        return !(end < min || start > max);
    }

    public float parseScore(String s) {
        if (s == null || s.length() == 0 || s.equals(".") || s.equals("-")) {
            return 0.0f;
        }
        return Float.parseFloat(s);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Sets.newHashSet("bed");
    }

    private Set<CompositionGlyph> convertBedFeaturesToCompositionGlyphs(Set<BedFeature> annotations) {
        Set<CompositionGlyph> primaryGlyphs = Sets.newLinkedHashSet();
        String[] label = {""};
        Map[] tooltipData = {Maps.newConcurrentMap()};
        annotations.stream().map((BedFeature annotation) -> {
            BedRenderer view = new BedRenderer();
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

    @Override
    public String getName() {
        return "Bed File";
    }

    @Override
    public Set<DataType> getDataTypes() {
        return Sets.newHashSet(DataType.ANNOTATION);
    }

    @Override
    public Set<CompositionGlyph> getRegion(String dataSourceReference, Range<Integer> range, Chromosome chromosome) {
        String chromosomeId = chromosome.getName();
        Set<BedFeature> annotations = Sets.newLinkedHashSet();
        try (LineIteratorImpl iterator = getLineIterator(dataSourceReference, range, chromosome)) {
            boolean alreadyTraversedRange = false; //assumes sorted bed file
            boolean alreadyTraversedChromosome = false;
            while (iterator.hasNext()) {
                String line = iterator.next().trim();
                List<String> fields = Splitter.on("\t").splitToList(line);
                String annotaionChromId = fields.get(0);
                annotaionChromId = chromosomeSynomymService.getPreferredChromosomeName(annotaionChromId).orElse(annotaionChromId);
                if (annotaionChromId.equals(chromosomeId)) {
                    final BedFeature bedFeature = createAnnotation(fields);
                    if (bedFeature.getRange().isConnected(range)) {
                        annotations.add(bedFeature);
                        alreadyTraversedRange = true;
                    } else if (alreadyTraversedRange) {
                        break;
                    }
                    alreadyTraversedChromosome = true;
                } else if (alreadyTraversedChromosome) {
                    break;
                }
            }
        } catch (IOException ex) {

        }
        return convertBedFeaturesToCompositionGlyphs(annotations);
    }

//    @Override
//    public Set<String> getSearchIndexKeys() {
//        return Sets.newHashSet("id");
//    }
//    @Override
//    public synchronized void createIndex(IndexIdentity indexIdentity, DataSourceReference dataSourceReference) {
//        final DataSource dataSource = dataSourceReference.getDataSource();
//        final String path = dataSourceReference.getPath();
//        List<Document> documents = Lists.newArrayList();
//        dataSource.getInputStream(path).ifPresent(inputStream -> {
//            String source = null;
//            try {
//                byte[] md5 = MessageDigest.getInstance("MD5").digest(dataSourceReference.getPath().getBytes("UTF-8"));
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < md5.length; i++) {
//                    sb.append(Integer.toString((md5[i] & 0xff) + 0x100, 16).substring(1));
//                }
//                source = sb.toString();
//            } catch (Exception ex) {
//                LOG.error(ex.getMessage(), ex);
//            }
//            LOG.info("Starting index {}", indexIdentity.getId());
//            LOG.info("clearing {}", source);
//
//            searchService.clearByQuery(indexIdentity, "source", source);
//            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
//                Iterator<String> iterator = bufferedReader.lines().iterator();
//                while (iterator.hasNext()) {
//                    String line = iterator.next().trim();
//                    List<String> fields = Splitter.on("\t").splitToList(line);
//                    final BedFeature bedFeature = createAnnotation(fields);
//                    if (bedFeature.getId().isPresent()) {
//                        Document document = new Document();
//                        document.getFields().put("id", bedFeature.getId().get());
//                        document.getFields().put("chromosomeId", bedFeature.getChromosomeId());
//                        document.getFields().put("start", bedFeature.getRange().lowerEndpoint().toString());
//                        document.getFields().put("end", bedFeature.getRange().upperEndpoint().toString());
//                        document.getFields().put("source", source);
//                        documents.add(document);
//                    }
//                    if (documents.size() > 10000 || !iterator.hasNext()) {
//                        searchService.index(documents, indexIdentity);
//                        documents.clear();
//                    }
//                }
//            } catch (Exception ex) {
//                LOG.error(ex.getMessage(), ex);
//            }
//            LOG.info("Completed index {}", indexIdentity.getId());
//        });
//    }
    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setChromosomeSynomymService(ChromosomeSynomymService chromosomeSynomymService) {
        this.chromosomeSynomymService = chromosomeSynomymService;
    }

}
