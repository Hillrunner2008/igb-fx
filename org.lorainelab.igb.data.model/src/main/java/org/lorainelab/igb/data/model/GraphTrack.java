package org.lorainelab.igb.data.model;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    private BooleanProperty isHeightLocked;
    private double lockedHeight;

    //average height
    private double modelHeight;
    private final DataSet dataSet;

    public GraphTrack(String trackLabel, DataSet dataSet) {
        this.isHeightLocked = new SimpleBooleanProperty(false);
        this.dataSet = dataSet;
        this.trackLabel = trackLabel;
        this.modelHeight = 10;
        this.lockedHeight = 200;
        glyphs = Lists.newArrayList();
    }

    @Override
    public void draw(GraphicsContext gc, View view, CanvasContext canvasContext) {
        double trackPositionOffset = canvasContext.getBoundingRect().getMinY() / view.getYfactor();
        gc.save();
        gc.translate(0, trackPositionOffset);
        glyphs.forEach(child -> child.getChildren().forEach(c -> c.draw(gc, view, view.modelCoordRect())));
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
        return glyphs;
    }

    @Override
    public void processSelectionRectangle(Rectangle2D selectionRectangle, View view) {
    }

    @Override
    public void clearGlyphs() {
        glyphs.clear();
    }

    @Override
    public void addGlyphs(Collection<CompositionGlyph> glyphs) {
        this.glyphs.addAll(glyphs);
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

    @Override
    public BooleanProperty isHeightLocked() {
        return isHeightLocked;
    }

    @Override
    public boolean allowLockToggle() {
        return false;
    }

}
