/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.api;

import org.lorainelab.igb.menu.api.model.WeightedMenu;

/**
 * This interface allows Apps to provide top level extensions to the menubar.
 * These "parent" menu entries will not be extensible by other apps.
 * @author dcnorris
 */
public interface MenuBarParentProvider {

    /**
     * @return The MenuItem that will be added to the IGB menubar. 
     */
    public WeightedMenu getParentMenu();

    /**
     * Weight
     * =====
     *
     * The weight property specifies the sorting of MenuItems.
     * A greater weight is always below or to the right of an element with a lower weight.
     *
     * @return menu weight
     */
    public int getWeight();
}
