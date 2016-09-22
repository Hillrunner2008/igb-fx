/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model;

import java.util.Collection;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;

/**
 *
 * @author dcnorris
 */
public interface Track {

    static final double MIN_TRACK_HEIGHT = 50;

    DataSet getDataSet();

    void draw(GraphicsContext gc, View view, CanvasContext canvasContext);

    void clearGlyphs();

    void addGlyphs(Collection<CompositionGlyph> glyphs);

    List<CompositionGlyph> getSelectedGlyphs();

    List<CompositionGlyph> getGlyphsInView(View view);

    void processSelectionRectangle(Rectangle2D selectionRectangle, View view);

    void clearSelections();

    boolean allowLockToggle();

    BooleanProperty isHeightLocked();

    double getLockedHeight();

    double getModelHeight();

    String getTrackLabel();

    default boolean isNegative() {
        return false;
    }

}
