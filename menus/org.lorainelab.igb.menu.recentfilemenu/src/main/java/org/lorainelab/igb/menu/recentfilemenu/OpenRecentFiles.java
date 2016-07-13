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
import com.google.common.collect.TreeMultimap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.data.model.datasource.DataSource;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandlerRegistry;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
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
    private WeightedMenuItem menuItem;
    private SelectionInfoService selectionInfoService;
    private SearchService searchService;
    private DataSource dataSource;
    private FileTypeHandlerRegistry fileTypeHandlerRegistry;
    private GenomeVersionRegistry genomeVersionRegistry;
    private CustomGenomePersistenceManager customGenomePersistenceManager;
    

    private static String md5Hash(String filePath) {
        HashCode hc = md5HashFunction.newHasher().putString(filePath, Charsets.UTF_8).hash();
        return hc.toString();
    }
    
    public OpenRecentFiles() {
        menuItem = new WeightedMenuItem(3, "Open Recent Files");
//        
//        Menu recentFilesMenu = new Menu("Open Recent Files");
//        recentFilesMenu.getItems().add(new MenuItem("Open Recent File"));
        
    }

    public static void OpenRecentFiles(String fileName) {
        md5HashFunction = Hashing.md5();
        modulePreferencesNode = PreferenceUtils.getPackagePrefsNode(OpenRecentFiles.class);
        recentFiles.add(fileName);
//        System.out.println("OpenRecentFiles: "+recentFiles);

        String nodeName = md5Hash(fileName);

        Preferences node = modulePreferencesNode.node(nodeName);
        node.put(FILE_NAME, fileName);
    }

    private void getRecentFiles(Preferences node, Menu recentFilesMenu) {

        String fileName = node.get(FILE_NAME, "");
        File file = new File(fileName);
        if (file.exists()) {
            System.out.println("fileName: " + fileName);

            recentFilesMenu.getItems().add(new MenuItem("'" + fileName + "'"));
//        menuItem = new WeightedMenuItem(6, recentFilesMenu);
            menuItem.setDisable(!selectionInfoService.getSelectedGenomeVersion().get().isPresent());
            selectionInfoService.getSelectedGenomeVersion().addListener((observable, oldValue, newValue) -> {
                menuItem.setDisable(!selectionInfoService.getSelectedGenomeVersion().get().isPresent());
            });
            menuItem.setOnAction(action -> openFileAction());
        }

    }

    @Activate
    public void activate() {
//        menuItem.getItems().add(new MenuItem("Test "));
        try {
            Menu recentFilesMenu = new Menu("Open Recent Files");
            Arrays.stream(modulePreferencesNode.childrenNames())
                    .map(nodeName -> modulePreferencesNode.node(nodeName))
                    .forEach(node -> {
                        getRecentFiles(node, recentFilesMenu);
                    });
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }

    }

    private void openFileAction() {
//        Optional.ofNullable(fileChooser.showOpenMultipleDialog(null)).ifPresent(selectedFiles -> {
//            selectedFiles.forEach(file -> {
//                fileTypeHandlerRegistry.getFileTypeHandlers().stream().filter(f -> {
//                    return f.getSupportedExtensions().contains(Files.getFileExtension(file.getPath()));
//                }).findFirst().ifPresent(fileTypeHandler -> {
//                    OpenRecentFiles.OpenRecentFiles(file.getPath());
//                    selectionInfoService.getSelectedGenomeVersion().get().ifPresent(gv -> {
//                        DataSourceReference dataSourceReference = new DataSourceReference(file.getPath(), dataSource);
//                        gv.getLoadedDataSets().add(new DataSet(file.getName(), dataSourceReference, fileTypeHandler));
//                        CompletableFuture<Void> indexTask = CompletableFuture.runAsync(() -> {
//                            try {
//                                Optional<GenomeVersion> genomeVersion = selectionInfoService.getSelectedGenomeVersion().getValue();
//                                if (genomeVersion.isPresent()) {
//                                    String speciesName = genomeVersion.get().getSpeciesName();
//                                    Optional<IndexIdentity> resourceIndexIdentity = searchService.getResourceIndexIdentity(speciesName);
//                                    if (!resourceIndexIdentity.isPresent()) {
//                                        LOG.info("index doesnt exist, so create it");
//                                        IndexIdentity indexIdentity = searchService.generateIndexIndentity();
//                                        searchService.setResourceIndexIdentity(speciesName, indexIdentity);
//                                        fileTypeHandler.createIndex(indexIdentity, dataSourceReference);
//                                    } else {
//                                        fileTypeHandler.createIndex(resourceIndexIdentity.get(), dataSourceReference);
//                                    }
//                                }
//                            } catch (Exception ex) {
//                                LOG.error(ex.getMessage(), ex);
//                            }
//                        }).whenComplete((result, ex) -> {
//                            if (ex != null) {
//                                LOG.error(ex.getMessage(), ex);
//                            }
//                        });
//
//                    });
//                });
//            });
//        });
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
    public Optional<List<WeightedMenuItem>> getMenuItems() {
        final List<WeightedMenuItem> menuItems = Lists.newArrayList(menuItem);
        return Optional.of(menuItems);
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.FILE;
    }

}
