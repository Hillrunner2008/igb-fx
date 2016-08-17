package org.lorainelab.igb.visualization.widget;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.Optional;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.visualization.ui.CanvasRegion;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.util.BoundsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = Widget.class)
public class SelectionRectangle implements Widget {

    private static final Logger LOG = LoggerFactory.getLogger(SelectionRectangle.class);
    private CanvasRegion canvasRegion;
    private CanvasModel canvasModel;
    private TracksModel tracksModel;

    public SelectionRectangle() {
    }

    @Override
    public void render(CanvasModel canvasModel) {
        GraphicsContext gc = canvasRegion.getCanvas().getGraphicsContext2D();
        try {
            gc.save();
            getSelectionRectangle().ifPresent(selectionRectangle -> {
                gc.setStroke(Color.RED);
                gc.strokeRect(selectionRectangle.getMinX(), selectionRectangle.getMinY(), selectionRectangle.getWidth(), selectionRectangle.getHeight());
            });
        } finally {
            gc.restore();
        }
    }

    private Point2D getRangeBoundedDragEventLocation(Point2D localPoint) {
        double boundedEventX = BoundsUtil.enforceRangeBounds(localPoint.getX(), 0, canvasRegion.getCanvas().getWidth());
        double boundedEventY = BoundsUtil.enforceRangeBounds(localPoint.getY(), 0, canvasRegion.getCanvas().getHeight());
        return new Point2D(boundedEventX, boundedEventY);
    }

    private Optional<Rectangle2D> getSelectionRectangle() {
        Rectangle2D[] selectionRectangle = new Rectangle2D[1];
        canvasModel.getMouseClickLocation().get().ifPresent(clickStartPosition -> {
            tracksModel.getCoordinateTrackRenderer().ifPresent(coordinateTrackRenderer -> {
                if (!coordinateTrackRenderer.getCanvasContext().getBoundingRect().contains(clickStartPosition)) {
                    canvasModel.getClickDragStartPosition().get().ifPresent(localPoint -> {
                        double minX;
                        double maxX;
                        double minY;
                        double maxY;
                        Point2D rangeBoundedEventLocation = getRangeBoundedDragEventLocation(localPoint);
                        if (clickStartPosition.getX() < rangeBoundedEventLocation.getX()) {
                            minX = clickStartPosition.getX();
                            maxX = rangeBoundedEventLocation.getX();
                        } else {
                            minX = rangeBoundedEventLocation.getX();
                            maxX = clickStartPosition.getX();
                        }
                        if (clickStartPosition.getY() < rangeBoundedEventLocation.getY()) {
                            minY = clickStartPosition.getY();
                            maxY = rangeBoundedEventLocation.getY();
                        } else {
                            minY = rangeBoundedEventLocation.getY();
                            maxY = clickStartPosition.getY();
                        }
                        selectionRectangle[0] = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
                    });
                }
            });
        });
        return Optional.ofNullable(selectionRectangle[0]);
    }

    @Reference
    public void setTracksModel(TracksModel tracksModel) {
        this.tracksModel = tracksModel;
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    @Reference
    public void setCanvasRegion(CanvasRegion canvasRegion) {
        this.canvasRegion = canvasRegion;
    }

    @Override
    public int getZindex() {
        return 10;
    }

}
