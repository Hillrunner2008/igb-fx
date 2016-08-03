package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.lorainelab.igb.visualization.component.SelectionRectangle.SelectionRectangleState;
import org.lorainelab.igb.visualization.component.api.Component;
import org.lorainelab.igb.visualization.component.api.State;
import org.lorainelab.igb.visualization.util.BoundsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class SelectionRectangle extends Component<SelectionRectangleProps, SelectionRectangleState> {

    private static final Logger LOG = LoggerFactory.getLogger(SelectionRectangle.class);

    public SelectionRectangle(SelectionRectangleProps props) {
        this.props = props;
    }

    @Override
    public Component beforeComponentReady() {
        return this;
    }

    @Override
    public List<Component> render() {
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
        return Lists.newArrayList();
    }

    private Point2D getRangeBoundedDragEventLocation(Point2D localPoint) {
        double boundedEventX = BoundsUtil.enforceRangeBounds(localPoint.getX(), 0, this.getProps().getCanvasPane().getWidth());
        double boundedEventY = BoundsUtil.enforceRangeBounds(localPoint.getY(), 0, this.getProps().getCanvasPane().getHeight());
        return new Point2D(boundedEventX, boundedEventY);
    }

    private Optional<Rectangle2D> getSelectionRectangle() {
        Rectangle2D[] selectionRectangle = new Rectangle2D[1];
        Optional.ofNullable(this.getProps().getClickStartPosition()).ifPresent(clickStartPosition -> {
            Optional.ofNullable(this.getProps().getLocalPoint()).ifPresent(localPoint -> {
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
        });
        return Optional.ofNullable(selectionRectangle[0]);
    }

    static class SelectionRectangleState implements State {

        static SelectionRectangleState factory() {
            return new SelectionRectangleState();
        }
    }
}
