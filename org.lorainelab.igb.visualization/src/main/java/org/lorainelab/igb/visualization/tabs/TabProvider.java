/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.tabs;

import javafx.scene.control.Tab;

/**
 *
 * @author dcnorris
 */
public interface TabProvider {

    Tab getTab();

    TabDockingPosition getTabDockingPosition();

    /**
     * Tabs will be sorted by integer weights Left to
     * Right for the bottom pane,and top to bottom on
     * the right pane.
     **/
    int getTabWeight();
}
