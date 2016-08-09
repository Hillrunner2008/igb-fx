package org.lorainelab.igb.visualization.component;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.Optional;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.visualization.PrimaryCanvasRegion;
import org.lorainelab.igb.visualization.model.CanvasPaneModel;
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
    private PrimaryCanvasRegion primaryCanvas;
    private CanvasPaneModel canvasPaneModel;
    private TracksModel tracksModel;

    public SelectionRectangle() {
    }

    @Override
    public void render(CanvasPaneModel canvasPaneModel) {
        GraphicsContext gc = primaryCanvas.getCanvas().getGraphicsContext2D();
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
        double boundedEventX = BoundsUtil.enforceRangeBounds(localPoint.getX(), 0, primaryCanvas.getCanvas().getWidth());
        double boundedEventY = BoundsUtil.enforceRangeBounds(localPoint.getY(), 0, primaryCanvas.getCanvas().getHeight());
        return new Point2D(boundedEventX, boundedEventY);
    }

    private Optional<Rectangle2D> getSelectionRectangle() {
        Rectangle2D[] selectionRectangle = new Rectangle2D[1];
        canvasPaneModel.getMouseClickLocation().get().ifPresent(clickStartPosition -> {
            tracksModel.getCoordinateTrackRenderer().ifPresent(coordinateTrackRenderer -> {
                if (!coordinateTrackRenderer.getCanvasContext().getBoundingRect().contains(clickStartPosition)) {
                    canvasPaneModel.getLocalPoint().get().ifPresent(localPoint -> {
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
    public void setCanvasPaneModel(CanvasPaneModel canvasPaneModel) {
        this.canvasPaneModel = canvasPaneModel;
    }

    @Reference
    public void setPrimaryCanvas(PrimaryCanvasRegion primaryCanvas) {
        this.primaryCanvas = primaryCanvas;
    }

    @Override
    public int getZindex() {
        return 10;
    }

}
