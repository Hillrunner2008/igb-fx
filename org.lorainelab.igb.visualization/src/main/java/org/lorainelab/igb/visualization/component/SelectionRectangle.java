package org.lorainelab.igb.visualization.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class SelectionRectangle  {

    private static final Logger LOG = LoggerFactory.getLogger(SelectionRectangle.class);

    public SelectionRectangle( ) {
    }

    public void render() {
//        GraphicsContext gc = this.getProps().getCanvasPane().getOverlayCanvas().getGraphicsContext2D();
//        try {
//            gc.save();
//            getSelectionRectangle().ifPresent(selectionRectangle -> {
//                gc.setStroke(Color.RED);
//                gc.strokeRect(selectionRectangle.getMinX(), selectionRectangle.getMinY(), selectionRectangle.getWidth(), selectionRectangle.getHeight());
//            });
//        } finally {
//            gc.restore();
//        }
    }

//    private Point2D getRangeBoundedDragEventLocation(Point2D localPoint) {
//        double boundedEventX = BoundsUtil.enforceRangeBounds(localPoint.getX(), 0, this.getProps().getCanvasPane().getWidth());
//        double boundedEventY = BoundsUtil.enforceRangeBounds(localPoint.getY(), 0, this.getProps().getCanvasPane().getHeight());
//        return new Point2D(boundedEventX, boundedEventY);
//    }

//    private Optional<Rectangle2D> getSelectionRectangle() {
//        Rectangle2D[] selectionRectangle = new Rectangle2D[1];
//        Optional.ofNullable(this.getProps().getClickStartPosition()).ifPresent(clickStartPosition -> {
//            Optional.ofNullable(this.getProps().getLocalPoint()).ifPresent(localPoint -> {
//                double minX;
//                double maxX;
//                double minY;
//                double maxY;
//                Point2D rangeBoundedEventLocation = getRangeBoundedDragEventLocation(localPoint);
//                if (clickStartPosition.getX() < rangeBoundedEventLocation.getX()) {
//                    minX = clickStartPosition.getX();
//                    maxX = rangeBoundedEventLocation.getX();
//                } else {
//                    minX = rangeBoundedEventLocation.getX();
//                    maxX = clickStartPosition.getX();
//                }
//                if (clickStartPosition.getY() < rangeBoundedEventLocation.getY()) {
//                    minY = clickStartPosition.getY();
//                    maxY = rangeBoundedEventLocation.getY();
//                } else {
//                    minY = rangeBoundedEventLocation.getY();
//                    maxY = clickStartPosition.getY();
//                }
//                selectionRectangle[0] = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
//            });
//        });
//        return Optional.ofNullable(selectionRectangle[0]);
//    }

}
