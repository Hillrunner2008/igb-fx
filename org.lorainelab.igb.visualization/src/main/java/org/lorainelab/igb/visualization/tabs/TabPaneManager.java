package org.lorainelab.igb.visualization.tabs;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.lorainelab.igb.tabs.api.TabProvider;
import static org.lorainelab.igb.visualization.util.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = TabPaneManager.class)
public class TabPaneManager {

    private static final Logger LOG = LoggerFactory.getLogger(TabPaneManager.class);
    private final TabPane rightTabPane;
    private final TabPane bottomTabPane;
    private Set<TabProvider> rightTabs;
    private Set<TabProvider> bottomTabs;
    private Map<TabPane, Set<TabProvider>> tabPositions;

    public TabPaneManager() {
        rightTabPane = new TabPane();
        bottomTabPane = new TabPane();
        setAnchorPaneConstraints(rightTabPane);
        setAnchorPaneConstraints(bottomTabPane);
        rightTabPane.setSide(Side.RIGHT);
        final Comparator<TabProvider> tabProviderComparator = Comparator.comparingInt(tp -> tp.getTabWeight());
        rightTabs = new TreeSet<>(tabProviderComparator);
        bottomTabs = new TreeSet<>(tabProviderComparator);
        tabPositions = new HashMap<>();
        tabPositions.put(rightTabPane, rightTabs);
        tabPositions.put(bottomTabPane, bottomTabs);
    }

    @Activate
    public void activate() {
    }

    @Reference(optional = true, multiple = true, unbind = "removeTab", dynamic = true)
    public void addTab(TabProvider tabProvider) {
        runAndWait(() -> {
            tabProvider.getTab().setClosable(false);
            switch (tabProvider.getTabDockingPosition()) {
                case BOTTOM:
                    addTab(bottomTabPane, tabProvider);
                    break;
                case RIGHT:
                    addTab(rightTabPane, tabProvider);
                    break;
            }
        });
    }

    public void removeTab(TabProvider tabProvider) {
        runAndWait(() -> {
            switch (tabProvider.getTabDockingPosition()) {
                case BOTTOM:
                    removeTab(bottomTabPane, tabProvider);
                    break;
                case RIGHT:
                    removeTab(rightTabPane, tabProvider);
                    break;
            }
        });
    }

    private synchronized void addTab(TabPane tabPane, TabProvider tabProvider) {
        final ObservableList<Tab> currentTabs = tabPane.getTabs();
        Set<TabProvider> sortedTabs = tabPositions.get(tabPane);
        sortedTabs.add(tabProvider);
        currentTabs.clear();
        sortedTabs.forEach(tp -> currentTabs.add(tp.getTab()));
    }

    private synchronized void removeTab(TabPane tabPane, TabProvider tabProvider) {
        final ObservableList<Tab> currentTabs = tabPane.getTabs();
        Set<TabProvider> sortedTabs = tabPositions.get(tabPane);
        sortedTabs.remove(tabProvider);
        currentTabs.clear();
        sortedTabs.forEach(tp -> currentTabs.add(tp.getTab()));
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

    @Deactivate
    public void deactivate() {
        LOG.info("TabPaneManager deactivated");
    }
}
