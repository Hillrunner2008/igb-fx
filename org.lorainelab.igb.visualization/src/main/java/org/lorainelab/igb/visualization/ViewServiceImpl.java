package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Range;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import org.lorainelab.igb.data.model.action.AbstractIgbAction;
import org.lorainelab.igb.data.model.action.IgbAction;
import org.lorainelab.igb.view.api.ViewService;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.widget.CoordinateTrackRenderer;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class ViewServiceImpl extends AbstractIgbAction implements ViewService, IgbAction {

    private CanvasModel canvasModel;
    private TracksModel tracksModel;

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

}
