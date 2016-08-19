/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model;

import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;

/**
 *
 * @author dcnorris
 */
public interface Track {

    void draw(GraphicsContext gc, View view, CanvasContext canvasContext);

    List<CompositionGlyph> getSelectedGlyphs();
    List<CompositionGlyph> getGlyphs();
    void processSelectionRectangle(Rectangle2D selectionRectangle, Rectangle2D viewBoundingRect);

    double getModelHeight();

    String getTrackLabel();
    
    default boolean isNegative(){return false;}
    
}
