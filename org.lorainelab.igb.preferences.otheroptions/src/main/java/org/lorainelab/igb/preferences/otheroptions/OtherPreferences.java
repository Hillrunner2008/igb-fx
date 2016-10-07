/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferences.otheroptions;

import aQute.bnd.annotation.component.Component;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import org.lorainelab.igb.preferencemanager.api.PreferencesTabProvider;
import org.lorainelab.igb.preferences.PreferenceUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true)
public class OtherPreferences implements PreferencesTabProvider {

    Button resetButton;
    Tab otherPrefTab;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(OtherPreferences.class);
  
    public OtherPreferences() {
        resetButton = new Button("Reset Preferences");
        resetButton.setOnAction(ae -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Preferences reset confirmation");
                alert.setHeaderText("Reset all preferences to default");
                alert.setContentText("Preferences reset needs application restart.\nAre you sure want reset preferences and exit?");
                alert.showAndWait().filter(resp -> resp == ButtonType.OK)
                        .ifPresent(resp -> {
                            LOG.info("Reset preferences requested.");
                            PreferenceUtils.clearAllPreferences();
                            LOG.debug("Preferences cleared, terminating the application");
                            System.exit(0);
                        });

            });
        });
        otherPrefTab = new Tab("Other Options");
        otherPrefTab.setContent(resetButton);
    }

    @Override
    public Tab getPreferencesTab() {
        return otherPrefTab;
    }

    @Override
    public int getTabWeight() {
        return 100;
    }

}
