/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.glyph;

import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.glyph.Glyph.SLOT_HEIGHT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author dcnorris
 */
public class GlyphTest {

    @Before
    public void setup() {

    }

    @Test
    public void fullyVisibleSlot() {
        GlyphImpl g = new GlyphImpl();
        g.setBoundingRect(new Rectangle2D(0, 0, 50, 10));
        Rectangle2D viewBoundingRect = new Rectangle2D(0, 0, 1000, 500);
        MockitoAnnotations.initMocks(this);
        View view = mock(View.class);
        when(view.getBoundingRect())
                .thenReturn(viewBoundingRect);
        when(view.getMutableBoundingRect())
                .thenReturn(new java.awt.geom.Rectangle2D.Double(viewBoundingRect.getMinX(), viewBoundingRect.getMinY(), viewBoundingRect.getWidth(), viewBoundingRect.getHeight()));
        when(view.isIsNegative())
                .thenReturn(false);
        Rectangle2D slotRect = new Rectangle2D(0, 470, 1000, SLOT_HEIGHT);
        Optional<java.awt.geom.Rectangle2D.Double> calculateDrawRect = g.calculateDrawRect(view, slotRect);
        assertTrue(calculateDrawRect.isPresent());
        java.awt.geom.Rectangle2D.Double drawRect = calculateDrawRect.get();
        assertEquals(drawRect.width, 50, 0);
        assertEquals(drawRect.height, 10, 0);
        //x-y/2 + slot offset
        final double centeredMinY = slotRect.getMinY() + (SLOT_HEIGHT - g.getBoundingRect().getHeight()) / 2;
        assertEquals(drawRect.y, centeredMinY, 0);
    }

    @Test
    public void partiallyVisibleSlotNoGlyphIntersectionInY() {
        GlyphImpl g = new GlyphImpl();
        g.setBoundingRect(new Rectangle2D(0, 0, 50, 10));
        Rectangle2D viewBoundingRect = new Rectangle2D(0, 0, 1000, 480);
        MockitoAnnotations.initMocks(this);
        View view = mock(View.class);
        when(view.getBoundingRect())
                .thenReturn(viewBoundingRect);
        when(view.getMutableBoundingRect())
                .thenReturn(new java.awt.geom.Rectangle2D.Double(viewBoundingRect.getMinX(), viewBoundingRect.getMinY(), viewBoundingRect.getWidth(), viewBoundingRect.getHeight()));
        when(view.isIsNegative())
                .thenReturn(false);
        Rectangle2D slotRect = new Rectangle2D(0, 470, 1000, SLOT_HEIGHT);
        Optional<java.awt.geom.Rectangle2D.Double> calculateDrawRect = g.calculateDrawRect(view, slotRect);
        assertFalse(calculateDrawRect.isPresent());
    }

    @Test
    public void partialGlyphIntersectionInYCutOffBottom() {
        GlyphImpl g = new GlyphImpl();
        g.setBoundingRect(new Rectangle2D(0, 0, 50, 20));
        Rectangle2D viewBoundingRect = new Rectangle2D(0, 0, 1000, 490);
        Rectangle2D slotRect = new Rectangle2D(0, 470, 1000, SLOT_HEIGHT);
        MockitoAnnotations.initMocks(this);
        View view = mock(View.class);
        when(view.getBoundingRect())
                .thenReturn(viewBoundingRect);
        when(view.getMutableBoundingRect())
                .thenReturn(new java.awt.geom.Rectangle2D.Double(viewBoundingRect.getMinX(), viewBoundingRect.getMinY(), viewBoundingRect.getWidth(), viewBoundingRect.getHeight()));
        when(view.isIsNegative())
                .thenReturn(false);
        Optional<java.awt.geom.Rectangle2D.Double> calculateDrawRect = g.calculateDrawRect(view, slotRect);
        assertTrue(calculateDrawRect.isPresent());
        java.awt.geom.Rectangle2D.Double drawRect = calculateDrawRect.get();
        assertEquals(drawRect.width, 50, 0);
        assertEquals(drawRect.height, 5, 0);
        //x-y/2 + slot offset
        final double centeredMinY = slotRect.getMinY() + (SLOT_HEIGHT - g.getBoundingRect().getHeight()) / 2;
        assertEquals(drawRect.y, centeredMinY, 0);
    }

    @Test
    public void partialGlyphIntersectionInYCutOffTop() {
        GlyphImpl g = new GlyphImpl();
        g.setBoundingRect(new Rectangle2D(0, 0, 50, 20));
        Rectangle2D viewBoundingRect = new Rectangle2D(0, 430, 1000, 500);
        Rectangle2D slotRect = new Rectangle2D(0, 410, 1000, SLOT_HEIGHT);
        MockitoAnnotations.initMocks(this);
        View view = mock(View.class);
        when(view.getBoundingRect())
                .thenReturn(viewBoundingRect);
        when(view.getMutableBoundingRect())
                .thenReturn(new java.awt.geom.Rectangle2D.Double(viewBoundingRect.getMinX(), viewBoundingRect.getMinY(), viewBoundingRect.getWidth(), viewBoundingRect.getHeight()));
        when(view.isIsNegative())
                .thenReturn(false);
        Optional<java.awt.geom.Rectangle2D.Double> calculateDrawRect = g.calculateDrawRect(view, slotRect);
        assertTrue(calculateDrawRect.isPresent());
        java.awt.geom.Rectangle2D.Double drawRect = calculateDrawRect.get();
        assertEquals(drawRect.width, 50, 0);
        assertEquals(drawRect.height, 15, 0);
        assertEquals(drawRect.y, 0, 0);
    }

    @Test
    public void partialGlyphIntersectionInXCutoffRight() {
        GlyphImpl g = new GlyphImpl();
        g.setBoundingRect(new Rectangle2D(0, 0, 50, 20));
        Rectangle2D viewBoundingRect = new Rectangle2D(0, 0, 45, 1000);
        Rectangle2D slotRect = new Rectangle2D(0, 470, 1000, SLOT_HEIGHT);
        MockitoAnnotations.initMocks(this);
        View view = mock(View.class);
        when(view.getBoundingRect())
                .thenReturn(viewBoundingRect);
        when(view.getMutableBoundingRect())
                .thenReturn(new java.awt.geom.Rectangle2D.Double(viewBoundingRect.getMinX(), viewBoundingRect.getMinY(), viewBoundingRect.getWidth(), viewBoundingRect.getHeight()));
        when(view.isIsNegative())
                .thenReturn(false);
        Optional<java.awt.geom.Rectangle2D.Double> calculateDrawRect = g.calculateDrawRect(view, slotRect);
        assertTrue(calculateDrawRect.isPresent());
        java.awt.geom.Rectangle2D.Double drawRect = calculateDrawRect.get();
        assertEquals(drawRect.width, 45, 0);
        assertEquals(drawRect.x, 0, 0);
        //x-y/2 + slot offset
        final double centeredMinY = slotRect.getMinY() + (SLOT_HEIGHT - g.getBoundingRect().getHeight()) / 2;
        assertEquals(drawRect.y, centeredMinY, 0);
    }

    @Test
    public void partialGlyphIntersectionInXCutoffLeft() {
        GlyphImpl g = new GlyphImpl();
        g.setBoundingRect(new Rectangle2D(0, 0, 50, 20));
        Rectangle2D viewBoundingRect = new Rectangle2D(5, 0, 50, 1000);
        MockitoAnnotations.initMocks(this);
        View view = mock(View.class);
        when(view.getBoundingRect())
                .thenReturn(viewBoundingRect);
        when(view.getMutableBoundingRect())
                .thenReturn(new java.awt.geom.Rectangle2D.Double(viewBoundingRect.getMinX(), viewBoundingRect.getMinY(), viewBoundingRect.getWidth(), viewBoundingRect.getHeight()));
        when(view.isIsNegative())
                .thenReturn(false);
        Rectangle2D slotRect = new Rectangle2D(0, 470, 1000, SLOT_HEIGHT);
        Optional<java.awt.geom.Rectangle2D.Double> calculateDrawRect = g.calculateDrawRect(view, slotRect);
        assertTrue(calculateDrawRect.isPresent());
        java.awt.geom.Rectangle2D.Double drawRect = calculateDrawRect.get();
        assertEquals(drawRect.width, 45, 0);
        assertEquals(drawRect.x, 0, 0);
        //x-y/2 + slot offset
        final double centeredMinY = slotRect.getMinY() + (SLOT_HEIGHT - g.getBoundingRect().getHeight()) / 2;
        assertEquals(drawRect.y, centeredMinY, 0);
    }

    public class GlyphImpl implements Glyph {

        private Rectangle2D boundingRect;

        public Color getFill() {
            return null;
        }

        public Color getStrokeColor() {
            return null;
        }

        public Rectangle2D getBoundingRect() {
            return boundingRect;
        }

        public void setBoundingRect(Rectangle2D boundingRect) {
            this.boundingRect = boundingRect;
        }

        public void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect) {
        }

        @Override
        public GlyphAlignment getGlyphAlignment() {
            return GlyphAlignment.CENTER;
        }

        @Override
        public void setGlyphAlignment(GlyphAlignment alignment) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
