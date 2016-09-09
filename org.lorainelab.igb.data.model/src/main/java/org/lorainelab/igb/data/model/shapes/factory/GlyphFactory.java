/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.shapes.factory;

import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.data.model.glyph.Glyph;
import org.lorainelab.igb.data.model.glyph.LineGlyph;
import org.lorainelab.igb.data.model.glyph.RectangleGlyph;

/**
 *
 * @author jeckstei
 */
public class GlyphFactory {
    final static Color DEFAULT_TEXT_COLOR = Color.BLACK;

    public static CompositionGlyph generateCompositionGlyph(String label, Map<String, String> tooltipData, List<Glyph> children) {
        CompositionGlyph cg = new CompositionGlyph(label, tooltipData, children);
        return cg;
    }

    public static RectangleGlyph generateRectangleGlyph(org.lorainelab.igb.data.model.shapes.Rectangle rectangle) {
        RectangleGlyph toReturn = new RectangleGlyph(rectangle);
        Color color = (Color) rectangle.getColor().orElse(Color.BLUE);
        toReturn.setFill(color);
        toReturn.setStrokeColor(DEFAULT_TEXT_COLOR);
        return toReturn;
    }

    public static LineGlyph generateLine(org.lorainelab.igb.data.model.shapes.Line line) {
        LineGlyph toReturn = new LineGlyph(line.getOffset(), line.getWidth());
        return toReturn;
    }
}
