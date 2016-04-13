package org.lorainelab.igb.demo;

import aQute.bnd.annotation.component.Activate;

public class DemoTrackRendererProvider {
//
//    private static final Logger LOG = LoggerFactory.getLogger(DemoTrackRendererProvider.class);
//    private Set<TrackRenderer> trackRenderers;
//    private ReferenceSequenceProvider refrenceSequenceProvider;
//    private CanvasPane canvasPane;
//    private Track negativeStrandTrack;
//    private Track positiveStrandTrack;

    public DemoTrackRendererProvider() {
//        trackRenderers = Sets.newLinkedHashSet();
    }

    @Activate
    public void activate() {
//        try {
//            modelWidth = refrenceSequenceProvider.getReferenceDna().length();
//            negativeStrandTrack = new Track(true, "RefGene (-)", 5);
//            positiveStrandTrack = new Track(false, "RefGene (+)", 5);
//            loadBedFileData();
//            ZoomableTrackRenderer bedFileTrack = new ZoomableTrackRenderer(canvasPane, positiveStrandTrack, modelWidth);
//            bedFileTrack.setWeight(0);
//            CoordinateTrackRenderer coordinateTrack = new CoordinateTrackRenderer(canvasPane, refrenceSequenceProvider);
//            coordinateTrack.setWeight(1);
//            ZoomableTrackRenderer negativeStrandBedFile = new ZoomableTrackRenderer(canvasPane, negativeStrandTrack, modelWidth);
//            negativeStrandBedFile.setWeight(2);
//            trackRenderers.add(bedFileTrack);
//            trackRenderers.add(coordinateTrack);
//            trackRenderers.add(negativeStrandBedFile);
//        } catch (IOException ex) {
//            LOG.error(ex.getMessage(), ex);
//        }
    }
//    private int modelWidth;
//
//    public Set<TrackRenderer> getTrackRenderers() {
//        return trackRenderers;
//    }
//
//    public int getModelWidth() {
//        return modelWidth;
//    }

//    private void loadBedFileData() throws IOException {
//        BedParser bedParser = new BedParser();
//        List<BedFeature> annotations = bedParser.getAnnotations();
//        List<CompositionGlyph> primaryGlyphs = Lists.newArrayList();
//
//        String[] label = {""};
//        Map[] tooltipData = {Maps.newConcurrentMap()};
//        annotations.stream().map((BedFeature annotation) -> {
//            BedRenderer view = new BedRenderer();
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
//                                    .isAssignableFrom(shape.getClass())) {
//                                children.add(GenovizFxFactory.generateRectangleGlyph((Rectangle) shape));
//
//                            }
//                            if (Line.class
//                                    .isAssignableFrom(shape.getClass())) {
//                                children.add(GenovizFxFactory.generateLine((Line) shape));
//                            }
//                        });
//                    });
//            primaryGlyphs.add(GenovizFxFactory.generateCompositionGlyph(label[0], tooltipData[0], children));
//        });
//        primaryGlyphs.stream().forEach(glyph -> {
//            if (glyph.getTooltipData().get("forward").equals("true")) {
//                positiveStrandTrack.getGlyphs().add(glyph);
//            } else {
//                negativeStrandTrack.getGlyphs().add(glyph);
//            }
//        });
//        //TODO this is a temporary hack for the demo... We will need the track to internally manage the slot data structure
//        positiveStrandTrack.buildSlots();
//        negativeStrandTrack.buildSlots();
//    }

//    private List<Shape> getShapes(Layer layer) {
//        List<Shape> toReturn = Lists.newArrayList();
//        layer.getItems().forEach(s -> {
//            if (s instanceof Layer) {
//                toReturn.addAll(getShapes((Layer) s));
//            } else {
//                toReturn.add(s);
//            }
//        });
//        return toReturn;
//    }
//
//    @Reference
//    public void setRefrenceSequenceProvider(ReferenceSequenceProvider refrenceSequenceProvider) {
//        this.refrenceSequenceProvider = refrenceSequenceProvider;
//    }
//
//    @Reference
//    public void setCanvasPane(CanvasPane canvasPane) {
//        this.canvasPane = canvasPane;
//    }
}
