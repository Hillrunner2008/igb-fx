/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.model;

import java.time.Duration;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.util.Either;
import static org.reactfx.EventStreams.eventsOf;

/**
 *
 * @author dcnorris
 */
public class CanvasContext {

    private final Canvas canvas;
    private Rectangle2D boundingRectangle;
    private double trackHeight;
    private double relativeTrackOffset;
    private EventStream<MouseEvent> mouseClickEventStream;
    private EventStream<MouseEvent> doubleClickEventStream;
    private boolean isVisible;

    CanvasContext(Canvas canvas, Rectangle2D boundingRectangle, double trackHeight, double relativeTrackOffset) {
        this.canvas = canvas;
        this.boundingRectangle = boundingRectangle;
        this.trackHeight = trackHeight;
        this.relativeTrackOffset = relativeTrackOffset;
        isVisible = false;
        setupMouseEventStreams();
    }

    private void setupMouseEventStreams() {
        mouseClickEventStream = EventStreams.eventsOf(canvas, MouseEvent.MOUSE_CLICKED).filter((e) -> boundingRectangle.contains(new Point2D(e.getX(), e.getY())));
        doubleClickEventStream = mouseClickEventStream.filter(event -> event.getClickCount() == 2);
        EventStream<MouseEvent> mouseEvents = eventsOf(canvas, MouseEvent.ANY);
        EventStream<Point2D> stationaryPositions = mouseEvents
                .successionEnds(Duration.ofSeconds(1))
                .filter(e -> e.getEventType() == MouseEvent.MOUSE_MOVED)
                .map(e -> {
                    return new Point2D(e.getScreenX(), e.getScreenY());

                });
        EventStream<Void> stoppers = mouseEvents.supply((Void) null);
        toolTipEventStream = stationaryPositions.or(stoppers)
                .distinct();
    }
    private EventStream<Either<Point2D, Void>> toolTipEventStream;

    public Rectangle2D getBoundingRect() {
        return boundingRectangle;
    }

    public GraphicsContext getGraphicsContext() {
        return canvas.getGraphicsContext2D();
    }

    public double getTrackHeight() {
        return trackHeight;
    }

    public double getRelativeTrackOffset() {
        return relativeTrackOffset;
    }

    public EventStream<MouseEvent> getMouseClickEventStream() {
        return mouseClickEventStream;
    }

    public EventStream<MouseEvent> getDoubleClickEventStream() {
        return doubleClickEventStream;
    }

    public EventStream<Either<Point2D, Void>> getTooltipEventStream() {
        return toolTipEventStream;
    }

    void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    void update(Rectangle2D boundingRectangle, double trackSize, double relativeTrackOffset) {
        this.boundingRectangle = boundingRectangle;
        this.trackHeight = trackSize;
        this.relativeTrackOffset = relativeTrackOffset;
    }
}
