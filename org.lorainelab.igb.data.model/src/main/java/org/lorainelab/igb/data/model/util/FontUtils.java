package org.lorainelab.igb.data.model.util;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 *
 * @author dcnorris
 */
public class FontUtils {

    private static RangeMap<Double, FontReference> fontReferences = fontReferences = TreeRangeMap.create();
    public static FontReference AXIS_LABEL_FONT;
    public static FontReference BASE_PAIR_FONT;
    public static String PREFERRED_FONT_BOLD = "ROBOTO MONO BOLD";
    public static String PREFERRED_FONT_MEDIUM = "ROBOTO MONO MEDIUM";
    public static String PREFERRED_FONT_NORMAL = "ROBOTO MONO REGULAR";

    static {
        BundleContext bc = FrameworkUtil.getBundle(FontUtils.class).getBundleContext();
        Font.loadFont(bc.getBundle().getEntry("RobotoMono-Bold.ttf").toExternalForm(), 12.0);
        Font.loadFont(bc.getBundle().getEntry("RobotoMono-Medium.ttf").toExternalForm(), 12.0);
        Font.loadFont(bc.getBundle().getEntry("RobotoMono-Regular.ttf").toExternalForm(), 12.0);
        AXIS_LABEL_FONT = new FontReference(Font.loadFont(bc.getBundle().getEntry("RobotoMono-Medium.ttf").toExternalForm(), 8));
        BASE_PAIR_FONT = new FontReference(Font.loadFont(bc.getBundle().getEntry("RobotoMono-Bold.ttf").toExternalForm(), 12.0));
    }

    static {
        double previousTextHeight = 0;
        for (int fontSize = 1; fontSize < 100; fontSize++) {
            Font font = Font.font(PREFERRED_FONT_MEDIUM, FontWeight.NORMAL, fontSize);
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
