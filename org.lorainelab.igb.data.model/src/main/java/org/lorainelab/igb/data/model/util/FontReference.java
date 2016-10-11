package org.lorainelab.igb.data.model.util;

import com.sun.javafx.tk.FontMetrics;
import javafx.scene.text.Font;

/**
 *
 * @author dcnorris
 */
public class FontReference {

    private Font font;
    private double textHeight;
    private final float ascent;
    private final float descent;
    private double lineHeight;

    public FontReference(Font font, FontMetrics fm) {
        this.font = font;
        this.textHeight = fm.getAscent() + fm.getDescent();
        this.ascent = fm.getAscent();
        this.descent = fm.getDescent();
        this.lineHeight = fm.getLineHeight();
    }

    public Font getFont() {
        return font;
    }

    public double getTextHeight() {
        return textHeight;
    }

    public double getLineHeight() {
        return lineHeight;
    }

    public float getAscent() {
        return ascent;
    }

    public float getDescent() {
        return descent;
    }

}
