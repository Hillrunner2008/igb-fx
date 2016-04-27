package org.lorainelab.igb.visualization.model;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.text.WordUtils;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.visualization.CanvasPane;
import org.lorainelab.igb.visualization.event.ClickDragEndEvent;
import org.lorainelab.igb.visualization.event.MouseClickedEvent;
import org.lorainelab.igb.visualization.event.MouseDoubleClickEvent;
import org.lorainelab.igb.visualization.event.MouseStationaryEndEvent;
import org.lorainelab.igb.visualization.event.MouseStationaryStartEvent;
import org.lorainelab.igb.visualization.event.RefreshTrackEvent;
import org.lorainelab.igb.visualization.event.ScrollXUpdate;
import org.lorainelab.igb.visualization.event.ZoomStripeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class ZoomableTrackRenderer implements TrackRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(ZoomableTrackRenderer.class);
    private TrackLabel trackLabel;
    final int modelWidth;
    final double modelHeight;
    final Track track;
    double zoomStripeCoordinate = -1;
    protected EventBus eventBus;
    private final View view;
    private final Tooltip tooltip = new Tooltip();
    private final CanvasContext canvasContext;
    private final GraphicsContext gc;
    private int weight;

    public ZoomableTrackRenderer(CanvasPane canvasPane, Track track, int modelCoordinatesGridSize) {
        this.weight = 0;
        this.eventBus = canvasPane.getEventBus();
        this.eventBus.register(this);
        this.track = track;
        this.modelWidth = modelCoordinatesGridSize;
        this.modelHeight = track.getModelHeight();
        view = new View(new Rectangle2D(0, 0, modelWidth, modelHeight));
        canvasContext = new CanvasContext(canvasPane.getCanvas(), Rectangle2D.EMPTY, 0, 0);
        trackLabel = new TrackLabel(this, track.getTrackLabel());
        gc = canvasPane.getCanvas().getGraphicsContext2D();
    }

    @Override
    public void updateView(double scrollX, double scrollY) {
        if (canvasContext.isVisible()) {
            final double visibleVirtualCoordinatesX = Math.floor(canvasContext.getBoundingRect().getWidth() / view.getXfactor());
            final double visibleVirtualCoordinatesY = Math.floor(canvasContext.getBoundingRect().getHeight() / view.getYfactor());
            double xOffset = Math.round((scrollX / 100) * (modelWidth - visibleVirtualCoordinatesX));
            if (zoomStripeCoordinate != -1) {
                double zoomStripePositionPercentage = (zoomStripeCoordinate - view.getBoundingRect().getMinX()) / view.getBoundingRect().getWidth();
                xOffset = Math.max(zoomStripeCoordinate - (visibleVirtualCoordinatesX * zoomStripePositionPercentage), 0);
                double maxXoffset = modelWidth - visibleVirtualCoordinatesX;
                xOffset = Math.min(maxXoffset, xOffset);
                final double calculatedScrollXPosition;
                if (maxXoffset > 0) {
                    calculatedScrollXPosition = (xOffset / (maxXoffset)) * 100;
                } else {
                    calculatedScrollXPosition = 0;
                }
                eventBus.post(new ScrollXUpdate(calculatedScrollXPosition));
            }
            double yOffset = canvasContext.getRelativeTrackOffset() / view.getYfactor();
            view.setBoundingRect(new Rectangle2D(xOffset, yOffset, visibleVirtualCoordinatesX, visibleVirtualCoordinatesY));
            render();
        }
    }

    @Override
    public void scaleCanvas(double xFactor, double scrollX, double scrollY) {
        if (canvasContext.isVisible()) {
            double scaleToY = canvasContext.getTrackHeight() / modelHeight;
            gc.save();
            gc.scale(xFactor, scaleToY);
            view.setXfactor(xFactor);
            view.setYfactor(scaleToY);
            gc.restore();
            updateView(scrollX, scrollY);
        }
    }

    private void clearCanvas() {
        gc.save();
        gc.clearRect(
                0,
                canvasContext.getBoundingRect().getMinY(),
                canvasContext.getBoundingRect().getWidth(),
                canvasContext.getBoundingRect().getHeight()
        );
        gc.setFill(Color.WHITE);
        gc.fillRect(
                0,
                canvasContext.getBoundingRect().getMinY(),
                canvasContext.getBoundingRect().getWidth(),
                canvasContext.getBoundingRect().getHeight()
        );
        gc.restore();
    }

    void draw() {
        gc.save();
        gc.scale(view.getXfactor(), view.getYfactor());
        track.draw(gc, view, canvasContext);
        gc.restore();
    }

    @Override
    public void render() {
        if (canvasContext.isVisible()) {
            clearCanvas();
            draw();
        }
    }

    private void hideTooltip() {
        Platform.runLater(() -> {
            tooltip.hide();
        });
    }

    private void showToolTip(Point2D local, Point2D screen) {

        Rectangle2D modelCoordinateBoundingBox = canvasToViewCoordinates(local);
        Optional<CompositionGlyph> intersect = track.getGlyphs().stream().filter(glyph -> glyph.getBoundingRect().intersects(modelCoordinateBoundingBox))
                .findFirst();

        if (intersect.isPresent()) {

            Platform.runLater(() -> {
                CompositionGlyph cg = intersect.get();
                StringBuilder sb = new StringBuilder();
                sb.append("id: ");
                sb.append(cg.getTooltipData().get("id"));
                sb.append("\n");
                sb.append("description: \n");
                sb.append(WordUtils.wrap(cg.getTooltipData().get("description"), 30, "\n", true));
                sb.append("\n");
                sb.append("--------------\n");
                cg.getTooltipData().keySet().stream()
                        .filter(key -> !key.equals("id") && !key.equals("description"))
                        .forEach(key -> {
                            sb.append(key);
                            sb.append(": ");
                            sb.append(cg.getTooltipData().get(key));
                            sb.append("\n");
                        });

                tooltip.setText(sb.toString());
                double newX = screen.getX() + 10;
                double newY = screen.getY() + 10;
                tooltip.show(gc.getCanvas(), newX, newY);
            });
        }
    }

    @Subscribe
    private void handleMouseStationaryStartEvent(MouseStationaryStartEvent event) {
        if (!canvasContext.getBoundingRect().contains(event.getLocal())) {
            return;
        }
        showToolTip(event.getLocal(), event.getScreen());
    }

    @Subscribe
    private void handleMouseStationaryEndEvent(MouseStationaryEndEvent event) {
        hideTooltip();
    }

    @Subscribe
    private void handleMouseClickEvent(MouseClickedEvent event) {
        if (!canvasContext.getBoundingRect().contains(event.getLocal())) {
            if (!event.isMultiSelectModeActive()) {
                clearSelections();
            }
            return;
        }
        Rectangle2D mouseEventBoundingBox = canvasToViewCoordinates(event.getLocal());
        if (!event.isMultiSelectModeActive()) {
            clearSelections();
        }
        track.getGlyphs().stream()
                .filter(glyph -> view.getBoundingRect().intersects(glyph.getBoundingRect()))
                .filter(glyph -> glyph.getBoundingRect().intersects(mouseEventBoundingBox))
                .forEach(glyph -> {
                    glyph.setIsSelected(true);
                });
        render();
    }

    @Subscribe
    private void handleMouseDoubleClickEvent(MouseDoubleClickEvent event) {
        if (canvasContext.isVisible()) {
            if (!canvasContext.getBoundingRect().contains(event.getLocal())) {
                return;
            }
            zoomStripeCoordinate = -1;
            track.getGlyphs().stream()
                    .filter(glyph -> glyph.isSelected())
                    .findFirst()
                    .ifPresent(t -> {
                        jumpZoom(t.getBoundingRect());
                    });
        }
    }

    @Subscribe
    private void handleRefreshTrackEvent(RefreshTrackEvent event) {
        render();
    }

    @Subscribe
    public void handleClickDragEndEvent(ClickDragEndEvent event) {
        clearSelections();
        if (canvasContext.isVisible()) {
            Rectangle2D selectionRectangle = event.getSelectionRectangle();
            if (canvasContext.getBoundingRect().intersects(selectionRectangle)) {
                Rectangle2D mouseEventBoundingBox = canvasToViewCoordinates(selectionRectangle);
                track.getGlyphs().stream()
                        .filter(glyph -> view.getBoundingRect().intersects(glyph.getBoundingRect()))
                        .filter(glyph -> glyph.getBoundingRect().intersects(mouseEventBoundingBox))
                        .forEach(glyph -> {
                            glyph.setIsSelected(true);
                        });
            }
        }
        render();
    }

    private void clearSelections() {
        track.getGlyphs().stream().forEach(glyph -> glyph.setIsSelected(false));
    }

    private Rectangle2D canvasToViewCoordinates(Point2D clickLocation) {
        double x = Math.floor(clickLocation.getX() / view.getXfactor());
        double y = Math.floor((clickLocation.getY() - canvasContext.getBoundingRect().getMinY()) / view.getYfactor());
        double offsetX = view.getBoundingRect().getMinX();
        double offsetY = view.getBoundingRect().getMinY();
        x += offsetX;
        y += offsetY;
        Rectangle2D mouseEventBoundingBox = new Rectangle2D(x, y, 1, 1);
        return mouseEventBoundingBox;
    }

    private Rectangle2D canvasToViewCoordinates(Rectangle2D localSelectionRectangle) {
        double minX = Math.floor(localSelectionRectangle.getMinX() / view.getXfactor());
        double maxX = Math.floor(localSelectionRectangle.getMaxX() / view.getXfactor());
        double minY = Math.floor((localSelectionRectangle.getMinY() - canvasContext.getBoundingRect().getMinY()) / view.getYfactor());
        double maxY = Math.floor((localSelectionRectangle.getMaxY() - canvasContext.getBoundingRect().getMinY()) / view.getYfactor());
        double offsetX = view.getBoundingRect().getMinX();
        double offsetY = view.getBoundingRect().getMinY();
        minX += offsetX;
        maxX += offsetX;
        minY += offsetY;
        maxY += offsetY;
        Rectangle2D mouseEventBoundingBox = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
        return mouseEventBoundingBox;
    }

    private void jumpZoom(Rectangle2D rect) {
        if (canvasContext.getBoundingRect() != null) {
            eventBus.post(new JumpZoomEvent(rect, this));
        }
    }

    @Subscribe
    private void zoomStripeListener(ZoomStripeEvent event) {
        zoomStripeCoordinate = event.getZoomStripeCoordinate();// / view.getXfactor();
    }

    @Override
    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public String getTrackLabelText() {
        return track.getTrackLabel();
    }

    @Override
    public int getModelWidth() {
        return modelWidth;
    }

    @Override
    public double getModelHeight() {
        return modelHeight;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public TrackLabel getTrackLabel() {
        return trackLabel;
    }

    public Track getTrack() {
        return track;
    }

}
