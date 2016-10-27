package org.lorainelab.igb.data.model.util;

import java.awt.Rectangle;
import org.lorainelab.igb.data.model.View;

/**
 *
 * @author dcnorris
 */
public class DrawUtils {

    //ensures width and height are at least 1 screen pixel
    public static void scaleToVisibleRec(View view, Rectangle.Double originalDrawRect) {
        double modelCoordinatesPerScreenXPixel = view.modelCoordRect().getWidth() / view.getCanvasContext().getBoundingRect().getWidth();
        double modelCoordinatesPerScreenYPixel = view.modelCoordRect().getHeight() / view.getCanvasContext().getBoundingRect().getHeight();
        double width = originalDrawRect.getWidth();
        double height = originalDrawRect.getHeight();
        if (width > 0 && width < modelCoordinatesPerScreenXPixel) {
            width = modelCoordinatesPerScreenXPixel;
        }
        if (height > 0 && height < modelCoordinatesPerScreenYPixel) {
            height = modelCoordinatesPerScreenYPixel;
        }
        originalDrawRect.setRect(originalDrawRect.getMinX(), originalDrawRect.getMinY(), width, height);
    }
}
