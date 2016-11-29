/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.opengenome;

import aQute.bnd.annotation.component.Component;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;

/**
 *
 * @author Devdatta
 */
@Component(immediate = true)
public class OpenGenomeMenuItem implements MenuBarEntryProvider {

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems() {
        final List<WeightedMenuEntry> menuItems = Lists.newArrayList(new WeightedMenuItem(1, "Open Genome\u2026"));
        return Optional.of(menuItems);
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.GENOME;
    }

}
