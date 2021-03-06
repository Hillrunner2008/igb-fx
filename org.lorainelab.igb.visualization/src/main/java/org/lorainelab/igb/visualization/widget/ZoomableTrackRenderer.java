package org.lorainelab.igb.visualization.widget;

import com.google.common.collect.Range;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import org.apache.commons.lang3.text.WordUtils;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import static org.lorainelab.igb.data.model.util.Palette.LOADED_REGION_BG;
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
    private DoubleProperty stretchDelta;
    private DoubleProperty activeStretchDelta;
    private SimpleBooleanProperty hideLockToggle;

    public ZoomableTrackRenderer(Canvas canvas, Track track, Chromosome chromosome) {
        this.weight = 0;
        this.track = track;
        this.chromosome = chromosome;
        this.modelWidth = chromosome.getLength();
        hideLockToggle = new SimpleBooleanProperty(track.allowLockToggle());
        stretchDelta = new SimpleDoubleProperty(0);
        activeStretchDelta = new SimpleDoubleProperty(0);
        canvasContext = new CanvasContext(canvas, DEFAULT_HEIGHT, 0);
        view = new View(new Rectangle2D(0, 0, modelWidth, track.getModelHeight()), canvasContext, chromosome, track.isNegative());
        trackLabel = new TrackLabel(this, track.getTrackLabel(), track.isHeightLocked(), track.isNegative());
        isHeightLocked = trackLabel.getIsHeightLocked();
        gc = canvas.getGraphicsContext2D();
        tooltip = new Tooltip();
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
            view.setModelCoordRect(new Rectangle2D(xOffset, yOffset, visibleVirtualCoordinatesX, visibleVirtualCoordinatesY));
            if (canvasContext.isVisible()) {
                draw(canvasModel);
            }
        }
    }

    private void scaleCanvas(CanvasModel canvasModel) {
        double xFactor = canvasModel.getxFactor().get();
        view.setXfactor(xFactor);
        if (canvasContext.isVisible()) {
            double scaleToY = canvasContext.getTrackHeight() / track.getModelHeight();
            if (Double.isInfinite(scaleToY)) {
                scaleToY = 1;
            }
            view.setYfactor(scaleToY);
            updateView(canvasModel);
        }
    }

    void draw(CanvasModel canvasModel) {
        if (Platform.isFxApplicationThread()) {
            gc.save();
            highlightLoadedRegions();
            gc.scale(view.getXfactor(), view.getYfactor());
            track.draw(gc, view, canvasContext);
            gc.restore();
        } else {
            LOG.error("Must be on FxApplication thread");
        }
    }

    private void highlightLoadedRegions() {
        gc.save();
        gc.scale(view.getXfactor(), 1);
        gc.setFill(LOADED_REGION_BG.get());

        track.getDataSet().getLoadedRegions(chromosome.getName()).asRanges().forEach(range -> {
            final Range<Integer> viewXRange = Range.closed(view.getXrange().lowerEndpoint().intValue(), view.getXrange().upperEndpoint().intValue());
            if (range.isConnected(viewXRange)) {
                Range<Integer> intersection = range.intersection(range);
                double minX = range.lowerEndpoint();
                final Bounds boundsInParent = getTrackLabel().getContent().getBoundsInParent();
                gc.fillRect(intersection.lowerEndpoint() - view.getXrange().lowerEndpoint(),
                        canvasContext.getBoundingRect().getMinY(),
                        intersection.upperEndpoint() - intersection.lowerEndpoint(),
                        canvasContext.getBoundingRect().getHeight()
                );
            }
        });
        gc.restore();
    }

    public void render(CanvasModel canvasModel) {
        processLastMouseClickedPosition(canvasModel);
        processSelectionRectangle(canvasModel);
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
        double y = Math.floor((clickLocation.getY() - canvasContext.getBoundingRect().getMinY()));
        double offsetX = view.modelCoordRect().getMinX();
        x += offsetX;
        Rectangle2D mouseEventBoundingBox = new Rectangle2D(x, y, 1, 1);
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

    @Override
    public Optional<Track> getTrack() {
        return Optional.of(track);
    }

    public boolean isContained(Point2D point) {
        return canvasContext.getBoundingRect().contains(point);
    }

    @Override
    public int getZindex() {
        return 1;
    }

    @Override
    public ReadOnlyBooleanProperty heightLocked() {
        return trackLabel.getIsHeightLocked();
    }

    @Override
    public DoubleProperty stretchDelta() {
        return stretchDelta;
    }

    @Override
    public DoubleProperty activeStretchDelta() {
        return activeStretchDelta;
    }

    @Override
    public BooleanProperty hideLockToggle() {
        return hideLockToggle;
    }

    private void processSelectionRectangle(CanvasModel canvasModel) {
        canvasModel.getSelectionRectangle().get().ifPresent(selectionRectangle -> {
            Rectangle2D canvascontextbound = canvasContext.getBoundingRect();
            convertAndTrimToViewCoords(selectionRectangle).ifPresent(rectangle -> {
                track.processSelectionRectangle(rectangle, view);
            });
        });
    }

    private Optional<Rectangle2D> convertAndTrimToViewCoords(Rectangle2D selectionRectangle) {
        Rectangle2D rectangle;
        track.getModelHeight();
        double minx = selectionRectangle.getMinX() / view.getXfactor();
        double maxx = selectionRectangle.getMaxX() / view.getXfactor();
        double xoffset = maxx - minx;
        double offsetX = view.modelCoordRect().getMinX();
        minx += offsetX;

        double boundingY = 0;
        double yHeight = 0;

        double trackMaxY = canvasContext.getBoundingRect().getMaxY();
        double trackMinY = canvasContext.getBoundingRect().getMinY();
        double selectionMaxY = Math.floor((selectionRectangle.getMaxY()));
        double selectionMinY = Math.floor((selectionRectangle.getMinY()));

        //entire track in selection
        if (trackMinY > selectionMinY && trackMaxY < selectionMaxY) {
            boundingY = trackMinY;
            yHeight = trackMaxY - boundingY;
        } //track top part in selection, botton out of selection
        else if (trackMinY > selectionMinY && trackMinY < selectionMaxY && trackMaxY > selectionMaxY) {
            boundingY = trackMinY;
            yHeight = selectionMaxY - boundingY;
        } //track top out of selection and bottom in selection
        else if (trackMinY < selectionMinY && trackMinY < selectionMaxY && trackMaxY < selectionMaxY && trackMaxY > selectionMinY) {
            boundingY = selectionMinY;
            yHeight = trackMaxY - boundingY;
        } //selection inside a track
        else if (trackMinY < selectionMinY && trackMaxY > selectionMaxY) {
            boundingY = selectionMinY;
            yHeight = selectionMaxY - selectionMinY;
        } else {
            return Optional.empty();
        }

        Rectangle2D mouseEventBoundingBox = new Rectangle2D(minx, Math.floor(boundingY - trackMinY), xoffset, Math.floor(yHeight));
        return Optional.of(mouseEventBoundingBox);
    }
}
