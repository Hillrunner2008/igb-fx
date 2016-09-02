package org.lorainelab.igb.visualization.widget;

import com.google.common.collect.Range;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.text.WordUtils;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.visualization.event.MouseHoverEvent;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TrackLabel;
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
    final Track track;
    private final View view;
    private final Tooltip tooltip;
    private final CanvasContext canvasContext;
    private final GraphicsContext gc;
    private int weight;
    private final Chromosome chromosome;
    private ReadOnlyBooleanProperty isHeightLocked;
    private double lockedHeight;

    public ZoomableTrackRenderer(Canvas canvas, Track track, Chromosome chromosome) {
        this.weight = 0;
        this.track = track;
        this.chromosome = chromosome;
        this.modelWidth = chromosome.getLength();
        canvasContext = new CanvasContext(canvas, track.getModelHeight(), 0);
        view = new View(new Rectangle2D(0, 0, modelWidth, track.getModelHeight()), canvasContext, chromosome, track.isNegative());
        trackLabel = new TrackLabel(this, track.getTrackLabel(), false);
        isHeightLocked = trackLabel.getIsHeightLocked();
        gc = canvas.getGraphicsContext2D();
        tooltip = new Tooltip();
        lockedHeight = track.getModelHeight();
        isHeightLocked.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                lockedHeight = canvasContext.getBoundingRect().getHeight();
            }
        });
    }

    private void processLastMouseClickedPosition(CanvasModel canvasModel) {
        boolean multiSelectModeActive = canvasModel.isMultiSelectModeActive().get();
        canvasModel.getMouseClickLocation().get().ifPresent(mouseClicked -> {
            if (!canvasContext.getBoundingRect().contains(mouseClicked)) {
                if (!multiSelectModeActive) {
                    track.clearSelections();
                }
                return;
            }
            Rectangle2D mouseEventBoundingBox = canvasToViewCoordinates(mouseClicked);
            if (!multiSelectModeActive) {
                track.clearSelections();
            }
            track.processSelectionRectangle(mouseEventBoundingBox, view);
        });
    }

    private void updateView(CanvasModel canvasModel) {
        double scrollX = canvasModel.getScrollX().get();
        if (canvasContext.isVisible()) {
            final double visibleVirtualCoordinatesX = Math.floor(canvasContext.getBoundingRect().getWidth() / view.getXfactor());
            final double visibleVirtualCoordinatesY = canvasContext.getBoundingRect().getHeight() / view.getYfactor();
            double xOffset = Math.round((scrollX / 100) * (modelWidth - visibleVirtualCoordinatesX));
            double yOffset = canvasContext.getRelativeTrackOffset() / view.getYfactor();
            view.setBoundingRect(new Rectangle2D(xOffset, yOffset, visibleVirtualCoordinatesX, visibleVirtualCoordinatesY));
            if (canvasContext.isVisible()) {
                draw(canvasModel);
            }
        }
    }

    private void scaleCanvas(CanvasModel canvasModel) {
        double xFactor = canvasModel.getxFactor().get();
//        double yFactor = canvasModel.getyFactor().get();
        view.setXfactor(xFactor);
//        view.setYfactor(yFactor);
        if (canvasContext.isVisible()) {
            if (isHeightLocked()) {
                double scaleToY = lockedHeight / track.getModelHeight();
                view.setYfactor(scaleToY);
            } else {
                double scaleToY = canvasContext.getTrackHeight() / track.getModelHeight();
                view.setYfactor(scaleToY);
            }
            updateView(canvasModel);
        }
    }

    void draw(CanvasModel canvasModel) {
        if (Platform.isFxApplicationThread()) {
            gc.save();
            gc.scale(view.getXfactor(), view.getYfactor());
//            highlightLoadedRegions();
            track.draw(gc, view, canvasContext);
            gc.restore();
        } else {
            LOG.error("Must be on FxApplication thread");
        }
    }

    private void highlightLoadedRegions() {
        gc.save();
        gc.setFill(Color.WHITE);
        track.getDataSet().getLoadedRegions(chromosome.getName()).asRanges().forEach(range -> {
            if (range.isConnected(Range.closed(view.getXrange().lowerEndpoint().intValue(), view.getXrange().upperEndpoint().intValue()))) {
                double minX = range.lowerEndpoint() - view.getXrange().lowerEndpoint();
                gc.fillRect(
                        minX,
                        canvasContext.getBoundingRect().getMinY(),
                        range.upperEndpoint() - range.lowerEndpoint(),
                        canvasContext.getBoundingRect().getHeight()
                );
            }
        });
        gc.restore();
    }

    public void render(CanvasModel canvasModel) {
        processLastMouseClickedPosition(canvasModel);
        scaleCanvas(canvasModel);
    }

    public void hideTooltip() {
        Platform.runLater(() -> {
            tooltip.hide();
        });
    }

    public void showToolTip(MouseHoverEvent hoverEvent) {
        Point2D local = hoverEvent.getLocal();
        Point2D screen = hoverEvent.getScreen();
        Rectangle2D modelCoordinateBoundingBox = canvasToViewCoordinates(local);
        Optional<CompositionGlyph> intersect = track.getGlyphsInView(view).stream()
                .filter(glyph -> glyph.getBoundingRect().intersects(modelCoordinateBoundingBox))
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
        return track.getModelHeight();
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

    public boolean isContained(Point2D point) {
        return canvasContext.getBoundingRect().contains(point);
    }

    @Override
    public int getZindex() {
        return 1;
    }

    @Override
    public boolean isHeightLocked() {
        return trackLabel.getIsHeightLocked().get();
    }

    public double getLockedHeight() {
        return lockedHeight;
    }
}
