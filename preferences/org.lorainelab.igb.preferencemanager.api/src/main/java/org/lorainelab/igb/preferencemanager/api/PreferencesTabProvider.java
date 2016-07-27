/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferencemanager.api;

import javafx.scene.control.Tab;

/**
 * This interface allows implementors to provide tabs in IGB preferences.
 *
 * @author Devdatta Kulkarni
 */
public interface PreferencesTabProvider {
  
    /**
     *
     * This method will be called when an App is initialized.
     *
     * @return a tab that has to be added to preferences.
     *
     */
     Tab getPreferencesTab();
     /**
     * @return the weight of tab. Tabs will be sorted on this weight
     */
     int getTabWeight();
}
