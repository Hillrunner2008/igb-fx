package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = MainViewerPane.class)
public class MainViewerPane extends VBox {

    private MainViewerPaneTopToolbar mainViewerPaneTopToolbar;
    private MainViewerSplitPane mainViewerSplitPane;

    @Activate
    public void activate() {
        VBox.setVgrow(mainViewerSplitPane, Priority.ALWAYS);
        Platform.runLater(() -> {
            getChildren().add(mainViewerPaneTopToolbar);
            getChildren().add(mainViewerSplitPane);
        });
    }

    @Reference
    public void setMainViewerPaneTopToolbar(MainViewerPaneTopToolbar mainViewerPaneTopToolbar) {
        this.mainViewerPaneTopToolbar = mainViewerPaneTopToolbar;
    }

    @Reference
    public void setMainViewerSplitPane(MainViewerSplitPane mainViewerSplitPane) {
        this.mainViewerSplitPane = mainViewerSplitPane;
    }

}
