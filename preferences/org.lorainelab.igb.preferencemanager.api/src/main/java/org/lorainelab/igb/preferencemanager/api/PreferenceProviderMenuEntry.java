/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferencemanager.api;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
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
        System.out.println("in constructor");
        menuItem = new WeightedMenuItem(30, "Preferences..");

    }

    @Activate
    public void activate() {
        System.out.println("in activate");
        Platform.runLater(() -> {
            initComponents();
            layoutComponents();
            menuItem.setOnAction(event -> {
                System.out.println("in runlater");
                Platform.runLater(() -> {
                    Platform.runLater(() -> {
                        stage.show();
                    });
                });
            });
        });
    }

    @Override
    public Optional<List<WeightedMenuItem>> getMenuItems() {
        List<WeightedMenuItem> menuItems = new ArrayList<WeightedMenuItem>();
        menuItems.add(menuItem);
        System.out.println("Reurned optional");
        return Optional.of(menuItems);

    }

    @Override
    public ParentMenu getParentMenu() {
        System.out.println("return parent");
        return ParentMenu.FILE;
    }

    @Reference
    public void setPreferencesTabManager(PreferencesTabManager preferencesTabManager) {
        this.preferencesTabManager = preferencesTabManager;
    }

    private void initComponents() {
        System.out.println(PreferencesTabManager.class + " init comp");
        pane = new TabPane();
        pane.setMinSize(800, 500);
        Tab t = new Tab("Hello Tab");
        t.setClosable(false);
        pane.getTabs().add(t);
        stage = new Stage();
        stage.sizeToScene();
        stage.centerOnScreen();

        stage.setResizable(true);
        stage.setTitle("Preferences");
    }

    private void layoutComponents() {
        System.out.println(PreferencesTabManager.class + "layout");
        stage.setScene(new Scene(pane));
    }
    

}
