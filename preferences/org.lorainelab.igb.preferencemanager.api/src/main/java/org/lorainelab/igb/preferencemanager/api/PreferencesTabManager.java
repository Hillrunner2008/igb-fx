/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferencemanager.api;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, provide = PreferencesTabManager.class)
public class PreferencesTabManager {

    private static final Logger LOG = LoggerFactory.getLogger(PreferencesTabManager.class);
    private SortedSet<PreferencesTabProvider> tabs;
    private TabPane pane;

    public PreferencesTabManager() {
        pane = new TabPane();
        tabs = new TreeSet<PreferencesTabProvider>((x,y)-> y.getTabWeight() - x.getTabWeight());
        setAnchorPaneConstraints(pane);
    }


    @Reference(optional = true, multiple = true, unbind = "removeTab", dynamic = true)
    public void addTab(PreferencesTabProvider tabProvider) {
        //pane.getTabs().add(tabProvider.getPreferencesTab());
        tabs.add(tabProvider);
    }

    public void removeTab(PreferencesTabProvider tabProvider) {
        //pane.getTabs().remove(tabProvider.getPreferencesTab());
        tabs.remove(tabProvider);
    }

    public TabPane getPreferencesTabPane(){
        pane.getTabs().clear();
        pane.getTabs().addAll(tabs.stream().map(tab -> tab.getPreferencesTab()).collect(Collectors.toList()));
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
