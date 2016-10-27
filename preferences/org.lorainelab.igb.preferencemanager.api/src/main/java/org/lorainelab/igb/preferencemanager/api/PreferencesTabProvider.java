package org.lorainelab.igb.preferencemanager.api;

import javafx.scene.control.Tab;

public interface PreferencesTabProvider {

    Tab getPreferencesTab();

    /**
     * @return the weight of tab.
     * Tabs will be sorted LEFT < RIGHT
     */
    int getTabWeight();
}
