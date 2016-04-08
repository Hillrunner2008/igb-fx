/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.api.util;

import org.lorainelab.igb.menu.api.model.MenuItem;

/**
 *
 * @author dcnorris
 */
public class MenuUtils {

    public static javafx.scene.control.MenuItem convertContextMenuItemToFxMenuItem(MenuItem menuItem) {
        javafx.scene.control.MenuItem fxMenuItem = new javafx.scene.control.MenuItem(menuItem.getMenuLabel());
        fxMenuItem.setOnAction(event -> {
            menuItem.getAction().apply(null);
        });
//        Optional<MenuIcon> menuItemIcon = menuItem.getMenuIcon();
//        if (menuItemIcon.isPresent()) {
//            fxMenuItem.setIcon(new ImageIcon(menuItemIcon.get().getEncodedImage()));
//        }
//        fxMenuItem.setEnabled(menuItem.isEnabled());
        return fxMenuItem;
    }
}
