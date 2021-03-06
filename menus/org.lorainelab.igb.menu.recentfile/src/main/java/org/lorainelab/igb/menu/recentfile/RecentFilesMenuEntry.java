package org.lorainelab.igb.menu.recentfile;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.MenuItem;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.datasetloadingservice.api.DataSetLoadingService;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.recentfiles.registry.api.RecentFilesRegistry;
import org.lorainelab.igb.selections.SelectionInfoService;
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
    private DataSetLoadingService fileOpener;
    private SelectionInfoService selectionInfoService;

    public RecentFilesMenuEntry() {
        recentFilesMenu = new WeightedMenu(RECENT_FILE_MENU_ENTRY_WEIGHT, "Open Recent Files");
        clearMenuItem = new MenuItem("Clear List");
    }

    @Activate
    public void activate() {
        buildRecentFileMenu();
        recentFilesRegistry.getRecentFiles().addListener(new WeakListChangeListener<>((ListChangeListener.Change<? extends String> change) -> {
            buildRecentFileMenu();
        }));
        clearMenuItem.setOnAction(action -> {
            recentFilesRegistry.clearRecentFiles();
            recentFilesMenu.setDisable(true);
        });
        recentFilesMenu.setDisable(!selectionInfoService.getSelectedGenomeVersion().get().isPresent());
        selectedGenomeVersionChangeListener = (observable, oldValue, newValue) -> {
            recentFilesMenu.setDisable(!selectionInfoService.getSelectedGenomeVersion().get().isPresent());
        };
        selectionInfoService.getSelectedGenomeVersion().addListener(new WeakChangeListener<>(selectedGenomeVersionChangeListener));
    }
    private ChangeListener<Optional<GenomeVersion>> selectedGenomeVersionChangeListener;

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

    private MenuItem createRecentFileMenuItem(String recentFile) {
        final MenuItem menuItem = new MenuItem(Files.getNameWithoutExtension(recentFile) + "." + Files.getFileExtension(recentFile));
        if (new File(recentFile).exists()) {
            menuItem.setOnAction(action -> {
                fileOpener.openDataSet(new File(recentFile));
            });
        } else {
            //option for user to delete entry
        }
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

    @Reference
    public void setDataSetLoadingService(DataSetLoadingService fileOpener) {
        this.fileOpener = fileOpener;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

}
