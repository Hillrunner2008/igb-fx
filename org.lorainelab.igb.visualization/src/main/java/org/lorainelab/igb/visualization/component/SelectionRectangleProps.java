package org.lorainelab.igb.visualization.component;

import javafx.geometry.Point2D;
import org.lorainelab.igb.visualization.CanvasPane;
import org.lorainelab.igb.visualization.component.api.Props;

/**
 *
 * @author dcnorris
 */
public class SelectionRectangleProps implements Props {

    private CanvasPane canvasPane;
    private final Point2D clickStartPosition;
    private final Point2D localPoint;

    public SelectionRectangleProps(CanvasPane canvasPane, Point2D clickStartPosition, Point2D localPoint) {
        this.canvasPane = canvasPane;
        this.localPoint = localPoint;
        this.clickStartPosition = clickStartPosition;
    }

    public CanvasPane getCanvasPane() {
        return canvasPane;
    }

    public Point2D getClickStartPosition() {
        return clickStartPosition;
    }

    public Point2D getLocalPoint() {
        return localPoint;
    }

}
