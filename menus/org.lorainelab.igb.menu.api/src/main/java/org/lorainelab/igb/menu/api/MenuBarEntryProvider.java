/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.api;

import java.util.List;
import java.util.Optional;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;

/**
 * This interface allows implementors to provide extentions to the IGB MenuBar menus.
 *
 * @module.info context-menu-api
 * @author dcnorris
 */
public interface MenuBarEntryProvider {

    /**
     *
     * This method will be called when an App is initialized.
     *
     * @return a list of MenuItem objects to be added to the menu bar menu.
     * It is possible an implementor will chose not to append any MenuItems
     * to the menu bar, and in this case should return Optional.empty() or
     * an empty list.
     *
     */
    public Optional<List<WeightedMenuEntry>> getMenuItems();

    /**
     * @return the ParentMenu this app will extend
     */
    public ParentMenu getParentMenu();
}
