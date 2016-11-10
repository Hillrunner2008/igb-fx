/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.recentgenome;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.Optional;
import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.recentgenome.registry.RecentGenomeRegistry;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta
 */
@Component(immediate = true)
public class RecentGenomeMenuEntry implements MenuBarEntryProvider {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RecentGenomeMenuEntry.class);
    private static final int RECENT_FILE_MENU_ENTRY_WEIGHT = 3;
    private RecentGenomeRegistry recentGenomeRegistry;
    private final WeightedMenu recentGenomeMenu;
    private final MenuItem clearMenuItem;
    private SelectionInfoService selectionInfoService;
    private GenomeVersionRegistry genomeVersionRegistry;

    public RecentGenomeMenuEntry() {
        recentGenomeMenu = new WeightedMenu(RECENT_FILE_MENU_ENTRY_WEIGHT, "Open Recent Genome");
        clearMenuItem = new MenuItem("Clear List");
    }

    @Activate
    public void activate() {
        buildRecentFileMenu();
        recentGenomeRegistry.getRecentGenomes().addListener((ListChangeListener.Change<? extends GenomeVersion> c) -> {
            buildRecentFileMenu();
        });
        clearMenuItem.setOnAction(action -> {
            recentGenomeRegistry.getRecentGenomes();
            recentGenomeMenu.setDisable(true);
        });
        
    }

    private void buildRecentFileMenu() {
        recentGenomeMenu.getItems().clear();
        recentGenomeRegistry.getRecentGenomes()
                .stream().map(recentGenome -> createRecentFileMenuItem(recentGenome))
                .forEach(menuItem -> recentGenomeMenu.getItems().add(menuItem));
        if (recentGenomeMenu.getItems().isEmpty()) {
            recentGenomeMenu.setDisable(true);
        } else {
            recentGenomeMenu.setDisable(false);
            recentGenomeMenu.getItems().add(clearMenuItem);
        }
    }

    private MenuItem createRecentFileMenuItem(GenomeVersion recentGenome) {
        String fileName = recentGenome.getReferenceSequenceProvider().getPath();
        final MenuItem menuItem = new MenuItem(recentGenome.getName().get());
        if (new File(fileName).exists()) {
            menuItem.setOnAction(action -> {
                //load genome
                genomeVersionRegistry.setSelectedGenomeVersion(recentGenome);
            });
        } else {
            //option for user to delete entry
        }
        return menuItem;
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems() {
        return Optional.ofNullable(Lists.newArrayList(recentGenomeMenu));
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.GENOME;
    }

    @Reference
    public void setRecentGenomeRegistry(RecentGenomeRegistry recentGenomeRegistry) {
        this.recentGenomeRegistry = recentGenomeRegistry;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }

}
