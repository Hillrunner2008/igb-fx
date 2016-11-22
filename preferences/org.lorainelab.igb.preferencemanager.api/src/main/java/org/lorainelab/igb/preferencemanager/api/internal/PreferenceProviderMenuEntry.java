/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferencemanager.api.internal;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true)
public class PreferenceProviderMenuEntry implements MenuBarEntryProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PreferenceProviderMenuEntry.class);
    private WeightedMenuItem menuItem;
    private TabPane pane;
    private Stage stage;
    private PreferencesTabManager preferencesTabManager;

    public PreferenceProviderMenuEntry() {
        menuItem = new WeightedMenuItem(30, "Preferences");
    }

    @Activate
    public void activate() {
        Platform.runLater(() -> {
            initComponents();
            menuItem.setOnAction(event -> {
                Platform.runLater(() -> {
                    stage.show();
                    stage.toFront();
                });
            });
        });
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems() {
        return Optional.of(Lists.newArrayList(menuItem));

    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.FILE;
    }

    @Reference
    public void setPreferencesTabManager(PreferencesTabManager preferencesTabManager) {
        this.preferencesTabManager = preferencesTabManager;
    }

    private void initComponents() {
        pane = preferencesTabManager.getPreferencesTabPane();
        pane.setMinSize(800, 500);
        stage = new Stage();
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setResizable(true);
        stage.setTitle("Preferences");
        stage.setScene(new Scene(pane));
    }

}
