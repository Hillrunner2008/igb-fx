/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.api.model;

import javafx.scene.control.MenuItem;

/**
 *
 * @author dcnorris
 */
public interface WeightedMenuEntry {

    int getWeight();
    
    MenuItem getMenuEntry();
    
}
