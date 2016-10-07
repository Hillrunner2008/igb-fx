package org.lorainelab.igb.data.model.util;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author dcnorris
 */
public class FontUtils {

    private static RangeMap<Double, FontReference> fontReferences = fontReferences = TreeRangeMap.create();

    static {
        double previousTextHeight = 0;
        for (int fontSize = 1; fontSize < 100; fontSize++) {
            Font font = Font.font("Monospaced", FontWeight.NORMAL, fontSize);
            FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);
            double textHeight = fm.getAscent() + fm.getDescent();
            fontReferences.put(Range.closed(previousTextHeight, textHeight), new FontReference(font, fm));
            previousTextHeight = textHeight;
        }
    }

    public static FontReference getFontByPixelHeight(double height) {
        final FontReference fontReference = fontReferences.get(height);
        if (fontReference == null) {
            if (height < 1) {
                return fontReferences.get(1d);
            }
            return fontReferences.get(99d);
        }
        return fontReference;
    }
}
