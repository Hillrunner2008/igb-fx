package org.lorainelab.igb.visualization.widget;

import com.google.common.collect.Range;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import javafx.application.Platform;
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
import org.lorainelab.igb.data.model.glyph.Glyph;
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

    public ZoomableTrackRenderer(Canvas canvas, Track track, Chromosome chromosome) {
        this.weight = 0;
        this.track = track;
        this.modelWidth = chromosome.getLength();
        view = new View(new Rectangle2D(0, 0, modelWidth, track.getModelHeight()), chromosome);
        canvasContext = new CanvasContext(canvas, 0, 0);
        trackLabel = new TrackLabel(this, track.getTrackLabel());
        gc = canvas.getGraphicsContext2D();
        tooltip = new Tooltip();
    }

    private void processLastMouseClickedPosition(CanvasModel canvasModel) {
        boolean multiSelectModeActive = canvasModel.isMultiSelectModeActive().get();
        canvasModel.getMouseClickLocation().get().ifPresent(mouseClicked -> {
            if (!canvasContext.getBoundingRect().contains(mouseClicked)) {
                if (!multiSelectModeActive) {
                    clearSelections();
                }
                return;
            }
            Rectangle2D mouseEventBoundingBox = canvasToViewCoordinates(mouseClicked);
            if (!multiSelectModeActive) {
                clearSelections();
            }
            Rectangle2D viewBoundingRect = view.getBoundingRect();
            List<CompositionGlyph> selections = track.getSlotMap().entrySet().stream().flatMap(entry -> {
                double slotOffset = track.getSlotOffset(entry.getKey());
                final Range<Double> mouseEventXrange = Range.closed(mouseEventBoundingBox.getMinX(), mouseEventBoundingBox.getMaxX());
                final Stream<CompositionGlyph> glyphsInXRange = entry.getValue().getGlyphsInXrange(mouseEventXrange).stream();
                return glyphsInXRange.filter(glyph -> {
                    final Range<Double> mouseEventYrange = Range.closed(mouseEventBoundingBox.getMinY(), mouseEventBoundingBox.getMaxY());
                    return Range.closed(glyph.getBoundingRect().getMinY() + slotOffset, glyph.getBoundingRect().getMaxY() + slotOffset).isConnected(mouseEventYrange);
                });
            }).collect(toList());
//
            if (selections.size() > 1) {
                selections.forEach(glyph -> glyph.setIsSelected(true));
            } else {
                selections.forEach(glyph -> {
                    boolean subSelectionActive = false;
                    for (Glyph g : glyph.getChildren()) {
                        if (g.isSelectable()) {
                            if (g.getBoundingRect().intersects(mouseEventBoundingBox)) {
                                g.setIsSelected(true);
                                subSelectionActive = true;
                                break;
                            }
                        }
                    }
                    glyph.setIsSelected(true);//set this flag regardless of subselection 
                });
            }

        });
    }

    private void updateView(double scrollX, double scrollY) {
        if (canvasContext.isVisible()) {
            final double visibleVirtualCoordinatesX = Math.floor(canvasContext.getBoundingRect().getWidth() / view.getXfactor());
            final double visibleVirtualCoordinatesY = Math.floor(canvasContext.getBoundingRect().getHeight() / view.getYfactor());
            double xOffset = Math.round((scrollX / 100) * (modelWidth - visibleVirtualCoordinatesX));
            double yOffset = canvasContext.getRelativeTrackOffset() / view.getYfactor();
            view.setBoundingRect(new Rectangle2D(xOffset, yOffset, visibleVirtualCoordinatesX, visibleVirtualCoordinatesY));
            if (canvasContext.isVisible()) {
                if (Platform.isFxApplicationThread()) {
                    clearCanvas();
                    draw();
                } else {
                    Platform.runLater(() -> {
                        clearCanvas();
                        draw();
                    });
                }
            }
        }
    }

    private void scaleCanvas(double xFactor, double scrollX, double scrollY) {
        view.setXfactor(xFactor);
        if (canvasContext.isVisible()) {
            double scaleToY = canvasContext.getTrackHeight() / track.getModelHeight();
            gc.save();
            gc.scale(xFactor, scaleToY);
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

    public void render(CanvasModel canvasModel) {
        clearCanvas();
        processLastMouseClickedPosition(canvasModel);
        scaleCanvas(canvasModel.getxFactor().get(), canvasModel.getScrollX().get(), canvasModel.getScrollY().get());
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
        Optional<CompositionGlyph> intersect = track.getSlotMap().values().stream()
                .flatMap(glyphBin -> glyphBin.getGlyphsInView(view).stream())
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

    private void clearSelections() {
        track.getSlotMap().values().stream()
                .flatMap(glyphBin -> glyphBin.getAllGlyphs().stream())
                .forEach(glyph -> {
                    glyph.setIsSelected(false);
                    for (Glyph g : glyph.getChildren()) {
                        g.setIsSelected(false);
                    }
                });
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
}
