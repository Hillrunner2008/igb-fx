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
import static org.lorainelab.igb.data.model.util.Palette.DEFAULT_GLYPH_FILL;

/**
 *
 * @author jeckstei
 */
public class GlyphFactory {

    public static CompositionGlyph generateCompositionGlyph(String label, Map<String, String> tooltipData, List<Glyph> children) {
        CompositionGlyph cg = new CompositionGlyph(label, tooltipData, children);
        return cg;
    }

    public static RectangleGlyph generateRectangleGlyph(org.lorainelab.igb.data.model.shapes.Rectangle rectangle) {
        RectangleGlyph toReturn = new RectangleGlyph(rectangle);
        Color color = (Color) rectangle.getColor().orElse(DEFAULT_GLYPH_FILL.get());
        toReturn.setFill(color);
        return toReturn;
    }

    public static LineGlyph generateLine(org.lorainelab.igb.data.model.shapes.Line line) {
        LineGlyph toReturn = new LineGlyph(line.getOffset(), line.getWidth());
        return toReturn;
    }
}
