package org.lorainelab.igb.visualization.footer;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.StatusBar;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = Footer.class)
public class Footer extends HBox {

    private static final double FIXED_HEIGHT = 25.0;
    private MemoryTracker memoryTracker;
    private Pane spacer;
    private StatusBarProvider statusBarProvider;

    public Footer() {
        setMaxHeight(FIXED_HEIGHT);
        setPrefHeight(FIXED_HEIGHT);
        memoryTracker = new MemoryTracker();
        spacer = new Pane();
        spacer.setMaxHeight(FIXED_HEIGHT);
        spacer.setPrefHeight(FIXED_HEIGHT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(memoryTracker, spacer);
    }

    @Reference(optional = true)
    public void setStatusBarProvider(StatusBarProvider statusBarProvider) {
        this.statusBarProvider = statusBarProvider;
        final StatusBar statusBar = statusBarProvider.getStatusBar();
        statusBar.setMaxWidth(500);
        statusBar.setPrefWidth(500);
        HBox.setMargin(statusBar, new Insets(2, 2, 2, 2));
        getChildren().addAll(statusBar);
    }

}
