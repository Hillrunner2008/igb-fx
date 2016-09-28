/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.widget;

import com.google.common.collect.Sets;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lorainelab.igb.data.model.CanvasContext;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TrackLabel;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.ui.VerticalScrollBar;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testfx.framework.junit.ApplicationTest;

/**
 *
 * @author dcnorris
 */
@RunWith(MockitoJUnitRunner.class)
public class LabelPaneTestFx extends ApplicationTest {

    @Mock
    private BundleContext bc = mock(BundleContext.class);

    @Mock
    private Bundle bundle = mock(Bundle.class);

    @Mock
    private ZoomableTrackRenderer posTr = mock(ZoomableTrackRenderer.class);
    @Mock
    private CoordinateTrackRenderer coordTr = mock(CoordinateTrackRenderer.class);
    @Mock
    private ZoomableTrackRenderer negTr = mock(ZoomableTrackRenderer.class);
    private CanvasContext posTrCanvasContext;
    private CanvasContext coordTrCanvasContext;
    private CanvasContext negTrCanvasContext;

    @Mock
    private CanvasModel canvasModel = mock(CanvasModel.class);
    @Mock
    private TracksModel tracksModel = mock(TracksModel.class);
    private TrackLabel pos;
    private TrackLabel coord;
    private TrackLabel neg;
    private LabelPane root;
    private Canvas canvas;

    @Override
    public void start(Stage primaryStage) throws Exception {
        canvas = new Canvas(250, 600);
        posTrCanvasContext = new CanvasContext(canvas, 275, 0);
        coordTrCanvasContext = new CanvasContext(canvas, 50, 275);
        negTrCanvasContext = new CanvasContext(canvas, 275, 325);
        posTrCanvasContext.setIsVisible(true);
        coordTrCanvasContext.setIsVisible(true);
        negTrCanvasContext.setIsVisible(true);
        root = new LabelPane();
        root.addEventFilter(ScrollEvent.ANY, (ScrollEvent event) -> event.consume());
        Scene scene = new Scene(root, canvas.getWidth(), canvas.getHeight());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Test
    public void basicTest() throws InterruptedException {
        MockitoAnnotations.initMocks(this);
        final SimpleBooleanProperty simpleBooleanProperty = new SimpleBooleanProperty(false);
        when(canvasModel.getLabelResizingActive()).thenReturn(simpleBooleanProperty);
        when(bc.getBundle()).thenReturn(bundle);

        when(posTr.getCanvasContext()).thenReturn(posTrCanvasContext);
        when(posTr.getWeight()).thenReturn(0);

        when(coordTr.getCanvasContext()).thenReturn(coordTrCanvasContext);
        when(coordTr.getWeight()).thenReturn(1);
        when(coordTr.heightLocked().get()).thenReturn(true);

        when(negTr.getCanvasContext()).thenReturn(negTrCanvasContext);
        when(negTr.getWeight()).thenReturn(2);

        pos = new TrackLabel(posTr, "bed(+)", new SimpleBooleanProperty(false));
        coord = new TrackLabel(coordTr, "Coordinates", new SimpleBooleanProperty(true));
        neg = new TrackLabel(negTr, "bed(-)", new SimpleBooleanProperty(false));
        
        when(posTr.getTrackLabel()).thenReturn(pos);
        when(coordTr.getTrackLabel()).thenReturn(coord);
        when(negTr.getTrackLabel()).thenReturn(neg);

        final ObservableSet<TrackRenderer> trackRenderers = FXCollections.observableSet(Sets.newHashSet(posTr, coordTr, negTr));
        when(tracksModel.getTrackRenderers()).thenReturn(trackRenderers);

        root.setTracksModel(tracksModel);
        root.setCanvasModel(canvasModel);
        root.setVerticalScrollBar(new VerticalScrollBar());
        runAndWait(() -> {
            root.render(canvasModel);
        });
        drag(pos.getResizeDragGrip(), MouseButton.PRIMARY)
                .moveBy(0, -50)
                .release(MouseButton.PRIMARY);
        drag(pos.getResizeDragGrip(), MouseButton.PRIMARY)
                .moveBy(0, 70)
                .release(MouseButton.PRIMARY);
        drag(neg.getResizeDragGrip(), MouseButton.PRIMARY)
                .moveBy(0, -50)
                .release(MouseButton.PRIMARY);
        drag(neg.getResizeDragGrip(), MouseButton.PRIMARY)
                .moveBy(0, 70)
                .release(MouseButton.PRIMARY);
        clickOn(pos.getUnLockIcon());
        clickOn(neg.getUnLockIcon());
        clickOn(pos.getLockIcon());
        clickOn(neg.getLockIcon());

        drag(pos.getDragGrip()).moveBy(20, 40).release(MouseButton.PRIMARY);
        drag(coord.getDragGrip()).moveBy(20, 40).release(MouseButton.PRIMARY);
        drag(neg.getDragGrip()).moveBy(20, 40).release(MouseButton.PRIMARY);
        Thread.sleep(100000);
    }
}
