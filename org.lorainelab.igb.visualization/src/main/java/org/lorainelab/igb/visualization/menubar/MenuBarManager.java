package org.lorainelab.igb.visualization.menubar;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import java.util.EnumMap;
import java.util.concurrent.ExecutionException;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import static org.lorainelab.igb.visualization.util.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = MenuBarManager.class)
public class MenuBarManager {

    private static final Logger LOG = LoggerFactory.getLogger(MenuBarManager.class);
    private final MenuBar menuBar;
    private final EnumMap<ParentMenu, Menu> parentMenuReference;
    private final TreeMultimap<Integer, Menu> parentMenuEntries;
    private final EnumMap<ParentMenu, SortedSetMultimap<Integer, MenuItem>> menuBarMenuContainer;
    private final TreeMultimap<Integer, MenuItem> fileMenuEntries;
    private final TreeMultimap<Integer, MenuItem> editMenuEntries;
    private final TreeMultimap<Integer, MenuItem> viewMenuEntries;
    private final TreeMultimap<Integer, MenuItem> toolsMenuEntries;
    private final TreeMultimap<Integer, MenuItem> helpMenuEntries;
    private final TreeMultimap<Integer, MenuItem> genomeMenuEntries;
    private Menu fileMenu;
    private Menu editMenu;
    private Menu viewMenu;
    private Menu toolsMenu;
    private Menu helpMenu;
    private Menu genomeMenu;
    private boolean componentActivated;

    public MenuBarManager() {
        componentActivated = false;
        menuBar = new MenuBar();
        fileMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        editMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        toolsMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        viewMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        helpMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        genomeMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        parentMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        menuBarMenuContainer = new EnumMap<>(ParentMenu.class);
        menuBarMenuContainer.put(ParentMenu.FILE, Multimaps.synchronizedSortedSetMultimap(fileMenuEntries));
        menuBarMenuContainer.put(ParentMenu.EDIT, Multimaps.synchronizedSortedSetMultimap(editMenuEntries));
        menuBarMenuContainer.put(ParentMenu.TOOLS, Multimaps.synchronizedSortedSetMultimap(toolsMenuEntries));
        menuBarMenuContainer.put(ParentMenu.VIEW, Multimaps.synchronizedSortedSetMultimap(viewMenuEntries));
        menuBarMenuContainer.put(ParentMenu.GENOME, Multimaps.synchronizedSortedSetMultimap(genomeMenuEntries));
        menuBarMenuContainer.put(ParentMenu.HELP, Multimaps.synchronizedSortedSetMultimap(helpMenuEntries));
        parentMenuReference = new EnumMap<>(ParentMenu.class);
        initializeMenus();
        initializeParentMenuReference();
        initializeParentMenuEntries();
    }

    @Activate
    public void activate() {
        componentActivated = true;
        rebuildMenus();
    }

    private void initializeMenus() {
        initFileMenu();
        initEditMenu();
        initViewMenu();
        initToolsMenu();
        initHelpMenu();
        initGenomeMenu();
    }

    private void initGenomeMenu() {
        genomeMenu = new Menu("Genome");
        genomeMenu.setMnemonicParsing(true);
    }

    private void initFileMenu() {
        fileMenu = new Menu("File");
        fileMenu.setMnemonicParsing(true);
        fileMenuEntries.put(5, new MenuItem("Load Url"));
        fileMenuEntries.put(10, new SeparatorMenuItem());
        fileMenuEntries.put(70, getExitMenuItem());
    }

    private MenuItem getExitMenuItem() {
        final MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(event -> System.exit(0));
        return exitMenuItem;
    }

    private void initEditMenu() {
        editMenu = new Menu("Edit");
        editMenu.setMnemonicParsing(true);
    }

    private void initViewMenu() {
        viewMenu = new Menu("View");
        viewMenu.setMnemonicParsing(true);
    }

    private void initToolsMenu() {
        toolsMenu = new Menu("Tools");
        toolsMenu.setMnemonicParsing(true);
    }

    private void initHelpMenu() {
        helpMenu = new Menu("Help");
        helpMenu.setMnemonicParsing(true);
    }

    private void initializeParentMenuReference() {
        parentMenuReference.put(ParentMenu.FILE, fileMenu);
        parentMenuReference.put(ParentMenu.EDIT, editMenu);
        parentMenuReference.put(ParentMenu.VIEW, viewMenu);
        parentMenuReference.put(ParentMenu.TOOLS, toolsMenu);
        parentMenuReference.put(ParentMenu.HELP, helpMenu);
        parentMenuReference.put(ParentMenu.GENOME, genomeMenu);
    }

    private void initializeParentMenuEntries() {
        parentMenuEntries.put(0, fileMenu);
        parentMenuEntries.put(1, editMenu);
        parentMenuEntries.put(5, viewMenu);
        parentMenuEntries.put(10, toolsMenu);
        parentMenuEntries.put(20, genomeMenu);
        parentMenuEntries.put(100, helpMenu);
    }

    @Reference(optional = true, multiple = true, unbind = "removeMenuBarExtension", dynamic = true)
    public void addMenuBarExtension(MenuBarEntryProvider menuBarExtension) {
        menuBarExtension.getMenuItems().ifPresent(menuItems -> {
            menuItems.stream()
                    .forEach(menuItem -> {
                        switch (menuBarExtension.getParentMenu()) {
                            case FILE:
                                fileMenuEntries.put(menuItem.getWeight(), menuItem.getMenuEntry());
                                break;
                            case EDIT:
                                editMenuEntries.put(menuItem.getWeight(), menuItem.getMenuEntry());
                                break;
                            case TOOLS:
                                toolsMenuEntries.put(menuItem.getWeight(), menuItem.getMenuEntry());
                                break;
                            case VIEW:
                                viewMenuEntries.put(menuItem.getWeight(), menuItem.getMenuEntry());
                                break;
                            case HELP:
                                helpMenuEntries.put(menuItem.getWeight(), menuItem.getMenuEntry());
                                break;
                            case GENOME:
                                genomeMenuEntries.put(menuItem.getWeight(), menuItem.getMenuEntry());
                                break;
                        }
                    });
        });
        if (componentActivated) {
            rebuildMenus();
        }
    }

    public void removeMenuBarExtension(MenuBarEntryProvider menuBarExtension) {
        menuBarExtension.getMenuItems().ifPresent(menuItems -> {
            menuItems.stream().forEach(menuItem -> {
                switch (menuBarExtension.getParentMenu()) {
                    case FILE:
                        removeMenuEntry(fileMenuEntries, menuItem);
                        break;
                    case EDIT:
                        removeMenuEntry(editMenuEntries, menuItem);
                        break;
                    case TOOLS:
                        removeMenuEntry(toolsMenuEntries, menuItem);
                        break;
                    case VIEW:
                        removeMenuEntry(viewMenuEntries, menuItem);
                        break;
                    case HELP:
                        removeMenuEntry(helpMenuEntries, menuItem);
                        break;
                    case GENOME:
                        removeMenuEntry(genomeMenuEntries, menuItem);
                        break;
                }
            });
        });
        if (componentActivated) {
            rebuildMenus();
        }
    }

    private void removeMenuEntry(TreeMultimap<Integer, MenuItem> menuEntryHolder, WeightedMenuEntry weightedMenuEntry) {
        if (menuEntryHolder.containsEntry(weightedMenuEntry.getWeight(), weightedMenuEntry.getMenuEntry())) {
            menuEntryHolder.remove(weightedMenuEntry.getWeight(), weightedMenuEntry.getMenuEntry());
        }
    }

    private synchronized void rebuildMenus() {
        try {
            runAndWait(() -> {
                rebuildFileMenu();
                rebuildEditMenu();
                rebuildViewMenu();
                rebuildToolsMenu();
                rebuildHelpMenu();
                rebuildGenomeMenu();
                rebuildParentMenus();
            });
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void rebuildParentMenus() {
        menuBar.getMenus().clear();
        parentMenuEntries.keySet().iterator().forEachRemaining(key -> {
            parentMenuEntries.get(key).forEach(menu -> menuBar.getMenus().add(menu));
        });
    }

    private void rebuildHelpMenu() {
        helpMenu.getItems().clear();
        helpMenuEntries.keySet().iterator().forEachRemaining(key -> {
            helpMenuEntries.get(key).forEach(menuItem -> helpMenu.getItems().add(menuItem));
        });
    }

    private void rebuildToolsMenu() {
        toolsMenu.getItems().clear();
        toolsMenuEntries.keySet().iterator().forEachRemaining(key -> {
            toolsMenuEntries.get(key).forEach(menuItem -> toolsMenu.getItems().add(menuItem));
        });
    }

    private void rebuildViewMenu() {
        viewMenu.getItems().clear();
        viewMenuEntries.keySet().iterator().forEachRemaining(key -> {
            viewMenuEntries.get(key).forEach(menuItem -> viewMenu.getItems().add(menuItem));
        });
    }

    private void rebuildEditMenu() {
        editMenu.getItems().clear();
        editMenuEntries.keySet().iterator().forEachRemaining(key -> {
            editMenuEntries.get(key).forEach(menuItem -> editMenu.getItems().add(menuItem));
        });
    }

    private void rebuildFileMenu() {
        fileMenu.getItems().clear();
        fileMenuEntries.keySet().iterator().forEachRemaining(key -> {
            fileMenuEntries.get(key).forEach(menuItem -> fileMenu.getItems().add(menuItem));
        });
    }

    private void rebuildGenomeMenu() {
        genomeMenu.getItems().clear();
        genomeMenuEntries.keySet().iterator().forEachRemaining(key -> {
            genomeMenuEntries.get(key).forEach(menuItem -> genomeMenu.getItems().add(menuItem));
        });
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    @Deactivate
    public void deactivate() {
        LOG.info("MenuBarManager deactivated");
    }
}
