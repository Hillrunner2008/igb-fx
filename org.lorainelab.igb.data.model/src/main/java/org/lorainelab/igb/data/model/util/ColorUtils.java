package org.lorainelab.igb.data.model.util;

import javafx.scene.paint.Color;

/**
 *
 * @author dcnorris
 */
public class ColorUtils {

    public static String colorToWebStyle(Color color) {
        return "-fx-background-color: " + "#" + Integer.toHexString(color.hashCode());
    }

    public static Color getEffectiveContrastColor(Color bgColor) {
        Color constractColor = null;
        if (null != bgColor) {
            double red = bgColor.getRed();
            double green = bgColor.getGreen();
            double blue = bgColor.getBlue();

            double yiq = ((red * 299) + (green * 587) + (blue * 114)) / 1000;
            constractColor = (yiq >= 128) ? Color.BLACK : Color.WHITE;
        }
        return constractColor;
    }

    public static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }

}
