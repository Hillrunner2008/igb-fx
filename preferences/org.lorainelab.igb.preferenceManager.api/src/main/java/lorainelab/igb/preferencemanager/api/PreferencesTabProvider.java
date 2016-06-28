/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lorainelab.igb.preferencemanager.api;

import javafx.scene.control.Tab;

/**
 *
 * @author Devdatta Kulkarni
 */
public interface PreferencesTabProvider {
    
     Tab getPreferencesTab();
     int getTabWeight();
}
