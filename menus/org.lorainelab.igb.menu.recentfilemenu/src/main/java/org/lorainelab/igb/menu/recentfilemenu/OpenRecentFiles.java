/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.recentfilemenu;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.data.model.datasource.DataSource;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandlerRegistry;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.menu.customgenome.CustomGenomePersistenceManager;
import org.lorainelab.igb.preferences.PreferenceUtils;
import static org.lorainelab.igb.menu.customgenome.CustomGenomePrefKeys.FILE_NAME;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.search.api.model.IndexIdentity;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author muralidhar
 */
@Component(immediate = true)
public class OpenRecentFiles implements MenuBarEntryProvider {

    public static Set<String> recentFiles = new HashSet<String>();
    private static final Logger LOG = LoggerFactory.getLogger(OpenRecentFiles.class);
    private static Preferences modulePreferencesNode;
    private static HashFunction md5HashFunction;
    private static int recentFilesCount = 0;
    private static WeightedMenuItem menuItem;
    private MenuItem recentFile;
    private SelectionInfoService selectionInfoService;
    private SearchService searchService;
    private DataSource dataSource;
    private FileTypeHandlerRegistry fileTypeHandlerRegistry;
    private FileTypeHandler fileTypeHandler;
    private GenomeVersionRegistry genomeVersionRegistry;
    private CustomGenomePersistenceManager customGenomePersistenceManager;
    private static WeightedMenu recentFilesMenu;

    private static String md5Hash(String filePath) {
        HashCode hc = md5HashFunction.newHasher().putString(filePath, Charsets.UTF_8).hash();
        return hc.toString();
    }

    public OpenRecentFiles() {
        modulePreferencesNode = PreferenceUtils.getPackagePrefsNode(OpenRecentFiles.class);
    }

    public void StoreRecentFiles(String fileName) throws BackingStoreException {
        recentFilesMenu.getItems().clear();
        md5HashFunction = Hashing.md5();
        modulePreferencesNode = PreferenceUtils.getPackagePrefsNode(OpenRecentFiles.class);
        String nodeName = md5Hash(fileName);
        Preferences node = modulePreferencesNode.node(nodeName);
        node.put(FILE_NAME, fileName);
        OpenRecentFilesMenu();
        recentFilesMenu.getItems().add(menuItem);
    }

    private static void fetchFilePreferences(Preferences node, Menu recentFilesMenu) {

        String fileName = node.get(FILE_NAME, "");
        if (!recentFiles.contains(fileName)) {
            recentFiles.add(fileName);
        }

    }

    private void OpenRecentFilesMenu() throws BackingStoreException {
        Arrays.stream(modulePreferencesNode.childrenNames())
                .map(nodeName -> modulePreferencesNode.node(nodeName))
                .forEach(node -> {
                    fetchFilePreferences(node, recentFilesMenu);
                });

        recentFiles.stream().forEach((file) -> {
            File f = new File(file);
            if (f.exists()) {
                recentFile = new MenuItem(f.getName());
                recentFilesMenu.getItems().add(recentFile);
                recentFile.setOnAction(action -> openFileAction(f));
            }
        });

        recentFilesCount = modulePreferencesNode.childrenNames().length;
        if (recentFilesCount == 0) {
            recentFilesMenu.setDisable(true);
        } else {
            recentFilesMenu.setDisable(false);
            menuItem.setOnAction(action -> clearRecentFiles());
        }
        
    }

    private void initRecentFileMenu() throws BackingStoreException {

        recentFilesMenu = new WeightedMenu(3, "Open Recent Files");
        menuItem = new WeightedMenuItem(recentFilesCount + 1, "Clear List");
        OpenRecentFilesMenu();
        recentFilesMenu.getItems().add(menuItem);

    }

    @Activate
    public void activate() throws BackingStoreException {
        initRecentFileMenu();
    }

    private void clearRecentFiles() {
        recentFiles.clear();
        try {
            Arrays.stream(modulePreferencesNode.childrenNames()).map(nodeName -> modulePreferencesNode.node(nodeName))
                    .forEach(node -> {
                        try {
                            node.removeNode();
                        } catch (BackingStoreException ex) {
                            java.util.logging.Logger.getLogger(OpenRecentFiles.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });

            OpenRecentFilesMenu();
        } catch (BackingStoreException ex) {
            java.util.logging.Logger.getLogger(OpenRecentFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void openFileAction(File file) {
        
        selectionInfoService.getSelectedGenomeVersion().get().ifPresent(gv -> {
            DataSourceReference dataSourceReference = new DataSourceReference(file.getPath(), dataSource);
            gv.getLoadedDataSets().add(new DataSet(file.getName(), dataSourceReference, fileTypeHandler));
            CompletableFuture<Void> indexTask = CompletableFuture.runAsync(() -> {
                try {
                    Optional<GenomeVersion> genomeVersion = selectionInfoService.getSelectedGenomeVersion().getValue();
                    if (genomeVersion.isPresent()) {
                        String speciesName = genomeVersion.get().getSpeciesName();
                        Optional<IndexIdentity> resourceIndexIdentity = searchService.getResourceIndexIdentity(speciesName);
                        if (!resourceIndexIdentity.isPresent()) {
                            LOG.info("index doesnt exist, so create it");
                            IndexIdentity indexIdentity = searchService.generateIndexIndentity();
                            searchService.setResourceIndexIdentity(speciesName, indexIdentity);
                            fileTypeHandler.createIndex(indexIdentity, dataSourceReference);
                        } else {
                            fileTypeHandler.createIndex(resourceIndexIdentity.get(), dataSourceReference);
                        }
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }).whenComplete((result, ex) -> {
                if (ex != null) {
                    LOG.error(ex.getMessage(), ex);
                }
            });

        });

    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }

    @Reference
    public void setCustomGenomePersistenceManager(CustomGenomePersistenceManager customGenomePersistenceManager) {
        this.customGenomePersistenceManager = customGenomePersistenceManager;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems() {
        final List<WeightedMenuEntry> menuItems = Lists.newArrayList(recentFilesMenu);
        return Optional.of(menuItems);
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.FILE;
    }

}
