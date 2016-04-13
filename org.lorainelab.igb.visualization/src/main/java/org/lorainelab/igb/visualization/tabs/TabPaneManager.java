package org.lorainelab.igb.visualization.tabs;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.lorainelab.igb.visualization.tabs.api.TabProvider;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = TabPaneManager.class)
public class TabPaneManager {

    private final TabPane rightTabPane;
    private final TabPane bottomTabPane;

    public TabPaneManager() {
        rightTabPane = new TabPane();
        bottomTabPane = new TabPane();
        setAnchorPaneConstraints(rightTabPane);
        setAnchorPaneConstraints(bottomTabPane);
        rightTabPane.setSide(Side.RIGHT);
    }

    @Activate
    public void activate() {
    }

    @Reference(optional = true, multiple = true, unbind = "removeTab", dynamic = true)
    public void addTab(TabProvider tabProvider) {
        Platform.runLater(() -> {
            switch (tabProvider.getTabDockingPosition()) {
                case BOTTOM:
                    bottomTabPane.getTabs().add(tabProvider.getTab());
                    break;
                case RIGHT:
                    rightTabPane.getTabs().add(tabProvider.getTab());
                    break;
            }
        });
    }

    public void removeTab(TabProvider tabProvider) {
        Platform.runLater(() -> {
            switch (tabProvider.getTabDockingPosition()) {
                case BOTTOM:
                    bottomTabPane.getTabs().remove(tabProvider.getTab());
                    break;
                case RIGHT:
                    rightTabPane.getTabs().remove(tabProvider.getTab());
                    break;
            }
        });
    }

    public TabPane getRightTabPane() {
        return rightTabPane;
    }

    public TabPane getBottomTabPane() {
        return bottomTabPane;
    }

    private void setAnchorPaneConstraints(TabPane pane) {
        double anchor = 0;
        AnchorPane.setBottomAnchor(pane, anchor);
        AnchorPane.setTopAnchor(pane, anchor);
        AnchorPane.setRightAnchor(pane, anchor);
        AnchorPane.setLeftAnchor(pane, anchor);
    }
}
