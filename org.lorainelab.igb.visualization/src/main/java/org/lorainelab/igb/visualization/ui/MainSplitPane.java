package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import org.lorainelab.igb.visualization.tabs.TabPaneManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = MainSplitPane.class)
public class MainSplitPane extends SplitPane {

    private static final Logger LOG = LoggerFactory.getLogger(MainSplitPane.class);
    private TabPaneManager tabPaneManager;
    private VerticalSplitPane verticalSplitPane;
    private AnchorPane leftSide;
    private AnchorPane rightSide;

    public MainSplitPane() {
        leftSide = new AnchorPane();
        rightSide = new AnchorPane();
        SplitPane.setResizableWithParent(rightSide, Boolean.FALSE);
        getItems().add(leftSide);
        getItems().add(rightSide);
        leftSide.setMinWidth(150);
        setDividerPositions(0.80);
    }

    @Activate
    public void activate() {
        AnchorPane.setBottomAnchor(verticalSplitPane, 0.0);
        AnchorPane.setLeftAnchor(verticalSplitPane, 0.0);
        AnchorPane.setRightAnchor(verticalSplitPane, 0.0);
        AnchorPane.setTopAnchor(verticalSplitPane, 0.0);
        leftSide.getChildren().add(verticalSplitPane);
        rightSide.getChildren().add(tabPaneManager.getRightTabPane());
    }

    @Reference(unbind = "removeTabPaneManager")
    public void setTabPaneManager(TabPaneManager tabPaneManager) {
        this.tabPaneManager = tabPaneManager;
    }

    public void removeTabPaneManager(TabPaneManager tabPaneManager) {
        LOG.info("removeTabPaneManager called");
        Platform.runLater(() -> {
            rightSide.getChildren().remove(tabPaneManager.getRightTabPane());
        });
    }

    @Reference(unbind = "removeVerticalSplitPane")
    public void setVerticalSplitPane(VerticalSplitPane verticalSplitPane) {
        this.verticalSplitPane = verticalSplitPane;
    }

    public void removeVerticalSplitPane(VerticalSplitPane verticalSplitPane) {
        Platform.runLater(() -> {
            leftSide.getChildren().remove(verticalSplitPane);
        });
    }

}
