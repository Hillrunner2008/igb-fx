/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferences.otheroptions;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import javafx.scene.control.Tab;
import org.lorainelab.igb.preferencemanager.api.PreferencesTabProvider;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true)
public class OtherPreferences implements PreferencesTabProvider{
 
    @Activate
    public void activate() {
    }
    
    @Override
    public Tab getPreferencesTab() {
        return new Tab("Other options");
    }

    @Override
    public int getTabWeight() {
        return 100;
    }
    
}
