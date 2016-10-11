package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import org.lorainelab.igb.visualization.tabs.TabPaneManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = VerticalSplitPane.class)
public class VerticalSplitPane extends SplitPane {

    private static final Logger LOG = LoggerFactory.getLogger(VerticalSplitPane.class);
    private TabPaneManager tabPaneManager;
    private MainViewerPane mainViewerPane;

    public VerticalSplitPane() {
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0.60);
    }

    @Activate
    public void activate() {
        Platform.runLater(() -> {
            final TabPane bottomTabPane = tabPaneManager.getBottomTabPane();
            SplitPane.setResizableWithParent(bottomTabPane, Boolean.FALSE);
            getItems().add(mainViewerPane);
            getItems().add(bottomTabPane);
        });
    }

    @Reference(unbind = "removeTabPaneManager")
    public void setTabPaneManager(TabPaneManager tabPaneManager) {
        this.tabPaneManager = tabPaneManager;
    }

    public void removeTabPaneManager(TabPaneManager tabPaneManager) {
        LOG.info("removeTabPaneManager called");
        Platform.runLater(() -> {
            getItems().remove(tabPaneManager.getBottomTabPane());
        });
    }

    @Reference
    public void setMainViewerPane(MainViewerPane mainViewerPane) {
        this.mainViewerPane = mainViewerPane;
    }

    public void unsetMainViewerPane(MainViewerPane mainViewerPane) {
        Platform.runLater(() -> {
            getItems().remove(mainViewerPane);
        });
    }
}
