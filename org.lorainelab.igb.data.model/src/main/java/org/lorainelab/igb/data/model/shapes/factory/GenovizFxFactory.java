/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.shapes.factory;

import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import static org.lorainelab.genoviz.fx.Palette.PRIMARY_TEXT_COLOR;
import org.lorainelab.genoviz.fx.model.CompositionGlyph;
import org.lorainelab.genoviz.fx.model.Glyph;
import org.lorainelab.genoviz.fx.model.LineGlyph;
import org.lorainelab.genoviz.fx.model.RectangleGlyph;

/**
 *
 * @author jeckstei
 */
public class GenovizFxFactory {

    public static CompositionGlyph generateCompositionGlyph(String label, Map<String, String> tooltipData, List<Glyph> children) {
        CompositionGlyph cg = new CompositionGlyph(label, tooltipData, children);
        return cg;
    }

    public static RectangleGlyph generateRectangleGlyph(org.lorainelab.igb.data.model.shapes.Rectangle rectangle) {
        int height = 10;
        if (rectangle.getAttributes().contains(org.lorainelab.igb.data.model.shapes.Rectangle.Attribute.thick)) {
            height = 15;
        }
        double y = height == 15 ? 17.5 : 20;
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
