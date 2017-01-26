package org.lorainelab.igb.data.model.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

/**
 *
 * @author dcnorris
 */
public class Palette {

    public static ObjectProperty<Color> DEFAULT_CANVAS_BG = new SimpleObjectProperty<>(Color.WHITE);
    public static ObjectProperty<Color> LOADED_REGION_BG = new SimpleObjectProperty<>(Color.WHITESMOKE);
    public static ObjectProperty<Color> DEFAULT_LINE_FILL = new SimpleObjectProperty<>(Color.BLACK);
    public static ObjectProperty<Color> DEFAULT_GLYPH_FILL = new SimpleObjectProperty<>(Color.web("#0084B4"));
    public static ObjectProperty<Color> DEFAULT_LABEL_COLOR = new SimpleObjectProperty<>(Color.BLACK);
    public static ObjectProperty<Color> SELECTION_COLOR = new SimpleObjectProperty<>(Color.web("#DC322F"));
    public static ObjectProperty<Color> GRAPH_GRID_FILL = new SimpleObjectProperty<>(Color.BLACK);
    public static ObjectProperty<Color> GRAPH_FILL = new SimpleObjectProperty<>(Color.web("#E24D42"));
    public static ObjectProperty<Color> CLICK_DRAG_HIGHLIGHT = new SimpleObjectProperty<>(Color.rgb(33, 150, 243, .3));
    public static ObjectProperty<Color> A_COLOR = new SimpleObjectProperty<>(Color.rgb(151, 255, 179));
    public static ObjectProperty<Color> T_COLOR = new SimpleObjectProperty<>(Color.rgb(102, 211, 255));
    public static ObjectProperty<Color> G_COLOR = new SimpleObjectProperty<>(Color.rgb(255, 210, 0));
    public static ObjectProperty<Color> C_COLOR = new SimpleObjectProperty<>(Color.rgb(255, 176, 102));

    private Palette() {
    }

    public static Color getBaseColor(char base) {
        switch (base) {
            case 'a':
            case 'A':
                return A_COLOR.get();
            case 't':
            case 'T':
                return T_COLOR.get();
            case 'g':
            case 'G':
                return G_COLOR.get();
            case 'c':
            case 'C':
                return C_COLOR.get();
            default:
                return Color.GRAY;
        }
    }
}
