package org.lorainelab.igb.visualization.ui;

import com.google.common.collect.Sets;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.embed.swing.JFXPanel;
import javafx.scene.canvas.Canvas;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.widget.CoordinateTrackRenderer;
import org.lorainelab.igb.visualization.widget.TrackRenderer;
import org.lorainelab.igb.visualization.widget.ZoomableTrackRenderer;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author dcnorris
 */
public class ViewPortManagerTest {

    @Mock
    private CanvasRegion canvasRegion = mock(CanvasRegion.class);
    @Mock
    private TracksModel tracksModel = mock(TracksModel.class);
    @Mock
    private Canvas canvas = mock(Canvas.class);
    @Mock
    private VerticalScrollBar verticalScrollBar = mock(VerticalScrollBar.class);

    @Mock
    private CanvasModel canvasModel = mock(CanvasModel.class);

    @BeforeClass
    public static void setup() {
        new JFXPanel();
    }

    @Before
    public void canvasModelStateTest() {
        vSliderValue = new SimpleDoubleProperty(0);
        DoubleProperty yFactor = new SimpleDoubleProperty(0);
        MockitoAnnotations.initMocks(this);
        when(canvasModel.getvSlider())
                .thenReturn(vSliderValue);
        when(canvasModel.getyFactor())
                .thenReturn(yFactor);
        canvas.setHeight(500d);
        canvas.setWidth(20_000_000d);
        final ObservableSet<TrackRenderer> testTrackRenderers = getTestTrackRenderers();
        when(tracksModel.getTrackRenderers()).thenReturn(testTrackRenderers);
        when(canvasRegion.getCanvas()).thenReturn(canvas);
        viewPortManager = new ViewPortManager();
        viewPortManager.setCanvasModel(canvasModel);
        viewPortManager.setTracksModel(tracksModel);
        viewPortManager.setVerticalScrollBar(verticalScrollBar);
        viewPortManager.setCanvasRegion(canvasRegion);
        assertEquals(canvas.getHeight(), 500, 0);
        assertEquals(canvas.getWidth(), 20_000_000, 0);
    }
    private DoubleProperty vSliderValue;

    private ZoomableTrackRenderer bottomTrack;
    private CoordinateTrackRenderer coordinateTrack;
    private ZoomableTrackRenderer topTrack;

    @Test
    public void testYFactor() {
        viewPortManager.activate();
        double yFactor = viewPortManager.getYFactor(450d);
        assertEquals(yFactor, 1d, 0);
        vSliderValue.set(100);
        yFactor = viewPortManager.getYFactor(450d);
        assertEquals(yFactor, 10d, 0);
        yFactor = viewPortManager.getYFactor(500d);
        assertEquals(yFactor, 10d, 0);
    }

    @Test
    public void testStretchToFit() {
        viewPortManager.activate();
        assertEquals(topTrack.getCanvasContext().getBoundingRect().getHeight(), 225d, 1);
        assertEquals(coordinateTrack.getCanvasContext().getBoundingRect().getHeight(), 50d, 0);
        assertEquals(bottomTrack.getCanvasContext().getBoundingRect().getHeight(), 225d, 1);
        assertEquals(verticalScrollBar.getMax(), 500d, 0);
    }

    @Test
    public void testFullyZoomed() {
        vSliderValue.set(100);
        viewPortManager.activate();
        assertEquals(topTrack.getCanvasContext().getBoundingRect().getHeight(), 500d, 1);
        assertFalse(coordinateTrack.getCanvasContext().isVisible());
        assertFalse(bottomTrack.getCanvasContext().isVisible());
        assertEquals(verticalScrollBar.getMax(), 4550d, 0);
        assertEquals(verticalScrollBar.getValue(), 0, 0);
        assertEquals(verticalScrollBar.getVisibleAmount(), 500, 0);
    }

    @Test
    public void testScrolledOffset() {
        vSliderValue.set(100);//zoom fully
        viewPortManager.activate();
        verticalScrollBar.setValue(4050d);
        viewPortManager.refresh();

        //validate visibility
        assertFalse(topTrack.getCanvasContext().isVisible());
        assertFalse(coordinateTrack.getCanvasContext().isVisible());
        assertEquals(bottomTrack.getCanvasContext().getBoundingRect().getHeight(), 500d, 1);

        //validate srollbar state
        assertEquals(verticalScrollBar.getMax(), 4550d, 0);
        assertEquals(verticalScrollBar.getValue(), 4050d, 0);
        assertEquals(verticalScrollBar.getVisibleAmount(), 500, 0);
    }

    @Test
    public void testCoordinatePartiallyInView() {
        vSliderValue.set(100);//zoom fully
        viewPortManager.activate();
        verticalScrollBar.setValue(2295d);
        viewPortManager.refresh();

        //validate srollbar state
        assertEquals(verticalScrollBar.getMax(), 4550d, 0);
        assertEquals(verticalScrollBar.getValue(), 2295d, 0);
        assertEquals(verticalScrollBar.getVisibleAmount(), 500, 0);

        //validate visibility
        assertFalse(topTrack.getCanvasContext().isVisible());
        assertTrue(coordinateTrack.getCanvasContext().isVisible());
        assertTrue(bottomTrack.getCanvasContext().isVisible());

        //validatePositions
        assertEquals(coordinateTrack.getCanvasContext().getBoundingRect().getHeight(), 5d, 1);
        assertEquals(bottomTrack.getCanvasContext().getBoundingRect().getHeight(), 495d, 1);

        assertEquals(coordinateTrack.getCanvasContext().getBoundingRect().getMinY(), 0d, 1);
        assertEquals(bottomTrack.getCanvasContext().getBoundingRect().getMinY(), 5, 1);
        
        assertEquals(coordinateTrack.getCanvasContext().getBoundingRect().getMaxY(), 5d, 1);
        assertEquals(bottomTrack.getCanvasContext().getBoundingRect().getMaxY(), 500d, 1);

    }

    private ViewPortManager viewPortManager;

    private ObservableSet<TrackRenderer> getTestTrackRenderers() {
        ObservableSet<TrackRenderer> testRenderers = FXCollections.observableSet(Sets.newHashSet());
        topTrack = mock(ZoomableTrackRenderer.class);
        coordinateTrack = mock(CoordinateTrackRenderer.class);
        bottomTrack = mock(ZoomableTrackRenderer.class);
        when(topTrack.isHeightLocked()).thenReturn(Boolean.FALSE);
        when(coordinateTrack.isHeightLocked()).thenReturn(Boolean.TRUE);
        when(bottomTrack.isHeightLocked()).thenReturn(Boolean.FALSE);

        when(topTrack.getWeight()).thenReturn(0);
        when(coordinateTrack.getWeight()).thenReturn(1);
        when(bottomTrack.getWeight()).thenReturn(2);

        when(coordinateTrack.getLockedHeight()).thenReturn(50d);

        when(topTrack.getModelHeight()).thenReturn(100d);
        when(coordinateTrack.getModelHeight()).thenReturn(50d);
        when(bottomTrack.getModelHeight()).thenReturn(100d);

        when(topTrack.getCanvasContext()).thenReturn(new CanvasContext(canvas, 100d, 0));
        when(coordinateTrack.getCanvasContext()).thenReturn(new CanvasContext(canvas, 50d, 0));
        when(bottomTrack.getCanvasContext()).thenReturn(new CanvasContext(canvas, 100d, 0));

        testRenderers.add(topTrack);
        testRenderers.add(coordinateTrack);
        testRenderers.add(bottomTrack);

        return testRenderers;
    }

}
