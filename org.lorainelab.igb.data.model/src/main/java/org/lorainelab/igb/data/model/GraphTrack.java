package org.lorainelab.igb.data.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.util.Collection;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.data.model.glyph.Glyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class GraphTrack implements Track {

    private static final Logger LOG = LoggerFactory.getLogger(GraphTrack.class);
    private final String trackLabel;
    private List<CompositionGlyph> glyphs;
    private RangeMap<Double, CompositionGlyph> intervalMap;

    //average height
    private double modelHeight;
    private final DataSet dataSet;

    public GraphTrack(String trackLabel, DataSet dataSet) {
        this.dataSet=dataSet;
        this.trackLabel = trackLabel;
        this.modelHeight = 10;
        glyphs = Lists.newArrayList();
        intervalMap = TreeRangeMap.create();
    }

    @Override
    public void draw(GraphicsContext gc, View view, CanvasContext canvasContext) {
        double trackPositionOffset = canvasContext.getBoundingRect().getMinY() / view.getYfactor();
        gc.save();
        //NOTE: Rounding issues prevent us from using translation to take care of view offsets in the x coordinate system
        // everything works fine until x coordinate get large, and then the larger numbers don't render correctly on the canvas
        //i.e. we can't do this gc.translate(-view.getBoundingRect().getMinX(), trackPositionOffset);
        gc.translate(0, trackPositionOffset);
        glyphs.forEach(child -> child.getChildren().forEach(c -> c.draw(gc, view, view.getBoundingRect())));
        gc.restore();
    }

    @Override
    public double getModelHeight() {
        return modelHeight;
    }

    @Override
    public String getTrackLabel() {
        return trackLabel;
    }

    @Override
    public List<CompositionGlyph> getSelectedGlyphs() {
        return glyphs.stream().filter(glyph -> glyph.isSelected()).collect(toList());
    }

    @Override
    public List<CompositionGlyph> getGlyphsInView(View view) {
        final List<CompositionGlyph> glyphsInRange;
        synchronized (intervalMap) {
            glyphsInRange = Lists.newArrayList(intervalMap.subRangeMap(view.getXrange()).asMapOfRanges().values());
        }
        return glyphsInRange;
    }

    @Override
    public void processSelectionRectangle(Rectangle2D selectionRectangle, View view) {
    }

    @Override
    public void clearGlyphs() {
        glyphs.clear();
        intervalMap.clear();
    }

    @Override
    public void addGlyphs(Collection<CompositionGlyph> glyphs) {
        this.glyphs.addAll(glyphs);
        glyphs.stream().forEach(glyph -> intervalMap.put(Range.closed(glyph.getBoundingRect().getMinX(), glyph.getBoundingRect().getMaxX()), glyph));
        modelHeight = glyphs.stream().mapToDouble(glyph -> glyph.getBoundingRect().getMaxY()).average().orElse(10);

    }

    @Override
    public void clearSelections() {
        glyphs.stream()
                .forEach(glyph -> {
                    glyph.setIsSelected(false);
                    for (Glyph g : glyph.getChildren()) {
                        g.setIsSelected(false);
                    }
                });
    }

    @Override
    public DataSet getDataSet() {
        return dataSet;
    }
}
