package org.lorainelab.igb.data.model.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.util.Comparator;
import java.util.Map;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author dcnorris
 */
public class FontUtils {

    private static RangeMap<Double, FontReference> fontReferences = fontReferences = TreeRangeMap.create();
    private static Map<String, Integer> preferredFontReference = Maps.newTreeMap();

    public static String PREFERRED_FONT_NAME = Font.getFamilies().stream()
            .filter(fontName -> preferredFontReference.containsKey(fontName))
            .sorted(Comparator.comparingInt(fontName -> preferredFontReference.get(fontName)))
            .findFirst()
            .orElse("");
    public static FontReference BASE_PAIR_FONT = new FontReference(Font.font(FontUtils.PREFERRED_FONT_NAME, FontWeight.BOLD, 12));
    public static FontReference AXIS_LABEL_FONT = new FontReference(Font.font("System", FontWeight.MEDIUM, 8));

    static {
        preferredFontReference.put("Courier New", 0);
        preferredFontReference.put("Courier", 1);
        preferredFontReference.put("Monospaced", 2);
        preferredFontReference.put("Ubuntu Mono", 3);
        preferredFontReference.put("FreeMono", 4);
        preferredFontReference.put("System", 5);

        double previousTextHeight = 0;
        for (int fontSize = 1; fontSize < 100; fontSize++) {
            Font font = Font.font(PREFERRED_FONT_NAME, FontWeight.NORMAL, fontSize);
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
