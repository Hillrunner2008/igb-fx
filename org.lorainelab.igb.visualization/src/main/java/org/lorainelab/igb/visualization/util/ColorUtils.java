package org.lorainelab.igb.visualization.util;

import javafx.scene.paint.Color;

/**
 *
 * @author dcnorris
 */
public class ColorUtils {

    public static String colorToWebStyle(Color color) {
        return "-fx-background-color: " + "#" + Integer.toHexString(color.hashCode());
    }

}
