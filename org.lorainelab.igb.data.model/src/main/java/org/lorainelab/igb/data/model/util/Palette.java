package org.lorainelab.igb.data.model.util;

import javafx.scene.paint.Color;

/**
 *
 * @author dcnorris
 */
public class Palette {

    public static Color DEFAULT_CANVAS_BG = Color.web("#323232");

    public static Color LOADED_REGION_BG = Color.web("#1E1E1E");
    public static Color DEFAULT_LINE_FILL = Color.web("#A9B7C6");
    public static Color DEFAULT_GLYPH_FILL = Color.web("#29938B");
    public static Color DEFAULT_LABEL_COLOR = Color.web("#A9B7C6");

    public static Color CLICK_DRAG_HIGHLIGHT = Color.rgb(33, 150, 243, .3);

    public static Color A_COLOR = Color.rgb(151, 255, 179);
    public static Color T_COLOR = Color.rgb(102, 211, 255);
    public static Color G_COLOR = Color.rgb(255, 210, 0);
    public static Color C_COLOR = Color.rgb(255, 176, 102);

    private Palette() {
    }

    public static Color getBaseColor(char base) {
        switch (base) {
            case 'a':
            case 'A':
                return A_COLOR;
            case 't':
            case 'T':
                return T_COLOR;
            case 'g':
            case 'G':
                return G_COLOR;
            case 'c':
            case 'C':
                return C_COLOR;
            default:
                return Color.GRAY;
        }
    }
}
