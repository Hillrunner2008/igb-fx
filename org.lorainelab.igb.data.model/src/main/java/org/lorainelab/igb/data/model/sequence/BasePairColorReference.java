package org.lorainelab.igb.data.model.sequence;

import javafx.scene.paint.Color;

/**
 *
 * @author dcnorris
 */
public class BasePairColorReference {

    private static final Color A_COLOR = Color.rgb(151, 255, 179);
    private static final Color T_COLOR = Color.rgb(102, 211, 255);
    private static final Color G_COLOR = Color.rgb(255, 210, 0);
    private static final Color C_COLOR = Color.rgb(255, 176, 102);

    private BasePairColorReference() {
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
