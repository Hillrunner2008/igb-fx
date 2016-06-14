package org.lorainelab.igb.menu.openfile;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.datasource.DataSource;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandlerRegistry;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.search.api.model.IndexIdentity;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.toolbar.api.ToolbarButtonProvider;
import org.lorainelab.igb.toolbar.api.WeightedButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class OpenFileMenuItem implements MenuBarEntryProvider, ToolbarButtonProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenFileMenuItem.class);
    private DataSource dataSource;
    private WeightedMenuItem menuItem;
    private WeightedButton openFileButton;
    private FileTypeHandlerRegistry fileTypeHandlerRegistry;
    private SelectionInfoService selectionInfoService;
    private SearchService searchService;

    @Activate
    public void activate() {
        menuItem = new WeightedMenuItem(1, "Load File");
        openFileButton = new WeightedButton(0, "", new FontAwesomeIconView(FontAwesomeIcon.FOLDER_OPEN));
        openFileButton.setOnAction(action -> openFileAction());
        menuItem.setDisable(!selectionInfoService.getSelectedGenomeVersion().get().isPresent());
        selectionInfoService.getSelectedGenomeVersion().addListener((observable, oldValue, newValue) -> {
            menuItem.setDisable(!selectionInfoService.getSelectedGenomeVersion().get().isPresent());
        });
        menuItem.setOnAction(action -> openFileAction());
    }

    private void openFileAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load File");
        File homeDirectory = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(homeDirectory);
        addFileExtensionFilters(fileChooser);
        Optional.ofNullable(fileChooser.showOpenMultipleDialog(null)).ifPresent(selectedFiles -> {
            selectedFiles.forEach(file -> {
                FileTypeHandler fileTypeHandler = fileTypeHandlerRegistry.getFileTypeHandlers().stream().findFirst().get();
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
            });
        });
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

    @Reference(target = "(&(component.name=" + "LocalDataSource" + "))")
    public void setLocalDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Reference
    public void setFileTypeHandlerRegistry(FileTypeHandlerRegistry fileTypeHandlerRegistry) {
        this.fileTypeHandlerRegistry = fileTypeHandlerRegistry;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private void addFileExtensionFilters(FileChooser fileChooser) {
        fileTypeHandlerRegistry.getFileTypeHandlers().stream()
                .map(fileTypeHandler -> {
                    return new FileChooser.ExtensionFilter(fileTypeHandler.getName(), fileTypeHandler.getSupportedExtensions().stream().map(ext -> "*." + ext).collect(Collectors.toList()));
                })
                .forEach(extensionFilter -> fileChooser.getExtensionFilters().add(extensionFilter));
    }

    @Override
    public WeightedButton getToolbarButton() {
        return openFileButton;
    }

}
