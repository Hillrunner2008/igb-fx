package org.lorainelab.igb.recentfiles.registry;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.util.List;
import java.util.Optional;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.MenuItem;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.recentfiles.registry.api.RecentFilesRegistry;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class RecentFilesMenuEntry implements MenuBarEntryProvider {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RecentFilesMenuEntry.class);
    private static final int RECENT_FILE_MENU_ENTRY_WEIGHT = 3;
    private static int RECENT_FILE_COUNT = 5;
    private RecentFilesRegistry recentFilesRegistry;
    private final WeightedMenu recentFilesMenu;
    private final MenuItem clearMenuItem;

    public RecentFilesMenuEntry() {
        recentFilesMenu = new WeightedMenu(RECENT_FILE_MENU_ENTRY_WEIGHT, "Open Recent Files");
        clearMenuItem = new MenuItem("Clear List");
    }

    @Activate
    public void activate() {
        buildRecentFileMenu();
        recentFilesRegistry.getRecentFiles().addListener((ListChangeListener.Change<? extends String> change) -> {
            buildRecentFileMenu();
        });
        clearMenuItem.setOnAction(action -> {
            recentFilesRegistry.clearRecentFiles();
            recentFilesMenu.setDisable(true);
        });
    }

    private void buildRecentFileMenu() {
        recentFilesMenu.getItems().clear();
        recentFilesRegistry.getRecentFiles()
                .stream().map(recentFile -> createRecentFileMenuItem(recentFile))
                .forEach(menuItem -> recentFilesMenu.getItems().add(menuItem));
        if (recentFilesMenu.getItems().isEmpty()) {
            recentFilesMenu.setDisable(true);
        } else {
            recentFilesMenu.setDisable(false);
            recentFilesMenu.getItems().add(clearMenuItem);
        }
    }

    private static MenuItem createRecentFileMenuItem(String recentFile) {
        final MenuItem menuItem = new MenuItem(Files.getNameWithoutExtension(recentFile) + "." + Files.getFileExtension(recentFile));
        menuItem.setOnAction(action -> {

        });
        return menuItem;
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems() {
        return Optional.ofNullable(Lists.newArrayList(recentFilesMenu));
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.FILE;
    }

    @Reference
    public void setRecentFilesRegistry(RecentFilesRegistry recentFilesRegistry) {
        this.recentFilesRegistry = recentFilesRegistry;
    }

}
