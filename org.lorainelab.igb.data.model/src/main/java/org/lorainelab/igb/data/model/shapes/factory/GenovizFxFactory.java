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
import static org.lorainelab.igb.data.model.glyph.Glyph.MODEL_HEIGHT_PADDING;
import org.lorainelab.igb.data.model.glyph.LineGlyph;
import org.lorainelab.igb.data.model.glyph.RectangleGlyph;
import static org.lorainelab.igb.data.model.shapes.factory.Palette.PRIMARY_TEXT_COLOR;

/**
 *
 * @author jeckstei
 */
public class GenovizFxFactory {

    public static CompositionGlyph generateCompositionGlyph(String label, Map<String, String> tooltipData, List<Glyph> children) {
        CompositionGlyph cg = new CompositionGlyph(label, tooltipData, children);
        return cg;
    }

    public static RectangleGlyph generateRectangleGlyph(org.lorainelab.igb.data.model.shapes.Rectangle rectangle ) {
        int height = 10;
        if (rectangle.getAttributes().contains(org.lorainelab.igb.data.model.shapes.Rectangle.Attribute.thick)) {
            height = 15;
        }
        double y = height == 15 ? MODEL_HEIGHT_PADDING : 20;
        RectangleGlyph toReturn = new RectangleGlyph(rectangle.getOffset(), y, rectangle.getWidth(), height);
        toReturn.setFill(Color.BLUE);
        toReturn.setStrokeColor(Color.web(PRIMARY_TEXT_COLOR));
        return toReturn;
    }

    public static LineGlyph generateLine(org.lorainelab.igb.data.model.shapes.Line line) {
        LineGlyph toReturn = new LineGlyph(line.getOffset(), line.getWidth(), 25);
        toReturn.setFill(Color.BLUE);
        toReturn.setStrokeColor(Color.web(PRIMARY_TEXT_COLOR));
        return toReturn;
    }
}
