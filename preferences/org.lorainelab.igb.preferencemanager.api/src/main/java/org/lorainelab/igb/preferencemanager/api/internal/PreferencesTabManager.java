/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferencemanager.api.internal;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.lorainelab.igb.preferencemanager.api.PreferencesTabProvider;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, provide = PreferencesTabManager.class)
public class PreferencesTabManager {

    private static final Logger LOG = LoggerFactory.getLogger(PreferencesTabManager.class);
    private List<PreferencesTabProvider> tabs;
    private TabPane pane;

    public PreferencesTabManager() {
        pane = new TabPane();
        tabs = Lists.newArrayList();
        setAnchorPaneConstraints(pane);
    }

    @Reference(optional = true, multiple = true, unbind = "removeTab", dynamic = true)
    public void addTab(PreferencesTabProvider tabProvider) {
        runAndWait(() -> {
            tabs.add(tabProvider);
            Collections.sort(tabs, Comparator.comparingInt(x -> x.getTabWeight()));
            pane.getTabs().clear();
            pane.getTabs().addAll(tabs.stream().map(tab -> tab.getPreferencesTab()).collect(Collectors.toList()));
        });
    }

    public void removeTab(PreferencesTabProvider tabProvider) {
        runAndWait(() -> {
            tabs.remove(tabProvider);
            Collections.sort(tabs, Comparator.comparingInt(x -> x.getTabWeight()));
            pane.getTabs().remove(tabProvider.getPreferencesTab());
        });
    }

    public TabPane getPreferencesTabPane() {
        return pane;
    }

    private void setAnchorPaneConstraints(TabPane pane) {
        double anchor = 0;
        AnchorPane.setBottomAnchor(pane, anchor);
        AnchorPane.setTopAnchor(pane, anchor);
        AnchorPane.setRightAnchor(pane, anchor);
        AnchorPane.setLeftAnchor(pane, anchor);
    }

}
