package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Range;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.data.model.action.AbstractIgbAction;
import org.lorainelab.igb.data.model.action.IgbAction;
import org.lorainelab.igb.view.api.ViewService;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.ui.CanvasRegion;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.invertExpScaleTransform;
import org.lorainelab.igb.visualization.widget.CoordinateTrackRenderer;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class ViewServiceImpl extends AbstractIgbAction implements ViewService, IgbAction {

    private CanvasModel canvasModel;
    private TracksModel tracksModel;
    private CanvasRegion canvasRegion;

    public ViewServiceImpl() {
        setText("Refresh Main View");
        setGraphic(new FontAwesomeIconView(FontAwesomeIcon.REFRESH));
    }

    @Override
    public Runnable getRefreshViewAction() {
        return canvasModel.getRefreshAction();
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    @Reference
    public void setTracksModel(TracksModel tracksModel) {
        this.tracksModel = tracksModel;
    }

    @Override
    public Optional<Range<Double>> getViewCoordinates() {
        final Optional<CoordinateTrackRenderer> coordinateTrackRenderer = tracksModel.getCoordinateTrackRenderer();
        if (coordinateTrackRenderer.isPresent()) {
            Rectangle2D boundingRect = coordinateTrackRenderer.get().getCanvasContext().getBoundingRect();
            return Optional.of(Range.closed(boundingRect.getMinX(), boundingRect.getMaxX()));
        }
        return Optional.empty();
    }

    @Override
    public void setViewCoordinateRange(Range<Double> range) {
        tracksModel.getCoordinateTrackRenderer().ifPresent(tr -> {
            Rectangle2D boundingRect = tr.getCanvasContext().getBoundingRect();
            View view = tr.getView();
            double modelWidth = canvasModel.getModelWidth().doubleValue();
            double minX = Math.max(range.lowerEndpoint(), view.modelCoordRect().getMinX());
            double maxX = Math.min(range.upperEndpoint(), view.modelCoordRect().getMaxX());
            double width = maxX - minX;

            double maxZoom = exponentialScaleTransform(canvasRegion.getWidth(), modelWidth, 100);
            double maxModelCoordinates = canvasRegion.getWidth() / maxZoom;
            if (width < maxModelCoordinates) {
                width = Math.max(width * 1.1, maxModelCoordinates);
                minX = Math.max((minX + range.upperEndpoint() - range.lowerEndpoint() / 2) - (width / 2), 0);
            }
            final double scaleXalt = tr.getCanvasContext().getBoundingRect().getWidth() / width;
            double scrollPosition = (minX / (modelWidth - width)) * 100;
            final double scrollXValue = enforceRangeBounds(scrollPosition, 0, 100);
            double newHSlider = invertExpScaleTransform(canvasRegion.getCanvas().getWidth(), canvasModel.getModelWidth().get(), scaleXalt);
            double xFactor = exponentialScaleTransform(canvasRegion.getCanvas().getWidth(), canvasModel.getModelWidth().get(), newHSlider);
            canvasModel.resetZoomStripe();
            canvasModel.setxFactor(xFactor);
            canvasModel.setScrollX(scrollXValue);
        });

    }

    @Reference
    public void setCanvasRegion(CanvasRegion canvasRegion) {
        this.canvasRegion = canvasRegion;
    }

}
