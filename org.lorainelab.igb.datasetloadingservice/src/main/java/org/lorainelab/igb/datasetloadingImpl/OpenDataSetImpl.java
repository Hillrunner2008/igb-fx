/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.datasetloadingImpl;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
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
import org.lorainelab.igb.preferences.SessionPreferences;
import org.lorainelab.igb.recentfiles.registry.api.RecentFilesRegistry;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.search.api.model.IndexIdentity;
import org.lorainelab.igb.selections.SelectionInfoService;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lorainelab.igb.datasetloadingservice.OpenDataSet;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, provide = OpenDataSet.class)
public class OpenDataSetImpl implements OpenDataSet {

    private static final Logger LOG = LoggerFactory.getLogger(OpenDataSetImpl.class);
    private static final String DEFAULT_FILE_EXTENSION_FILTER_NAME = "All Supported Formats";

    private DataSource dataSource;
    private FileTypeHandlerRegistry fileTypeHandlerRegistry;
    private SelectionInfoService selectionInfoService;
    private SearchService searchService;
    private RecentFilesRegistry recentFilesRegistry;

    @Override
    public void openFile() {
        openFileAction();
    }

    @Override
    public void openFile(File file) {
        fileTypeHandlerRegistry.getFileTypeHandlers().stream().filter(f -> {
            return f.getSupportedExtensions().contains(Files.getFileExtension(file.getPath()));
        }).findFirst().ifPresent(fileTypeHandler -> {
            selectionInfoService.getSelectedGenomeVersion().get().ifPresent(gv -> {
                recentFilesRegistry.getRecentFiles().add(file.getPath());
                DataSourceReference dataSourceReference = new DataSourceReference(file.getPath(), dataSource);
                gv.getLoadedDataSets().add(new DataSet(file.getName(), dataSourceReference, fileTypeHandler));
                indexDataSetForSearch(fileTypeHandler, dataSourceReference);
            });
        });
    }

    private void openFileAction() {
        FileChooser fileChooser = getFileChooser();
        Optional.ofNullable(fileChooser.showOpenMultipleDialog(null)).ifPresent(selectedFiles -> {
            selectedFiles.forEach(file -> {
                fileTypeHandlerRegistry.getFileTypeHandlers().stream().filter(f -> {
                    return f.getSupportedExtensions().contains(Files.getFileExtension(file.getPath()));
                }).findFirst().ifPresent(fileTypeHandler -> {
                    selectionInfoService.getSelectedGenomeVersion().get().ifPresent(gv -> {
                        recentFilesRegistry.getRecentFiles().add(file.getPath());
                        DataSourceReference dataSourceReference = new DataSourceReference(file.getPath(), dataSource);
                        gv.getLoadedDataSets().add(new DataSet(file.getName(), dataSourceReference, fileTypeHandler));
                        indexDataSetForSearch(fileTypeHandler, dataSourceReference);
                    });
                });
            });
        });
    }

    private FileChooser getFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load File");
        File homeDirectory;
        if (SessionPreferences.getRecentSelectedFilePath() != null) {
            File file = new File(SessionPreferences.getRecentSelectedFilePath());
            String simpleFileName = file.getParent();
            homeDirectory = new File(simpleFileName);
        } else {
            homeDirectory = new File(System.getProperty("user.home"));
        }
        fileChooser.setInitialDirectory(homeDirectory);
        addFileExtensionFilters(fileChooser);
        return fileChooser;
    }

    private void indexDataSetForSearch(FileTypeHandler fileTypeHandler, DataSourceReference dataSourceReference) {
        CompletableFuture<Void> indexTask = CompletableFuture.runAsync(() -> {
            try {
                Optional<GenomeVersion> genomeVersion = selectionInfoService.getSelectedGenomeVersion().getValue();
                if (genomeVersion.isPresent()) {
                    String speciesName = genomeVersion.get().getSpeciesName().get();
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
    }

    private void addFileExtensionFilters(FileChooser fileChooser) {
        fileTypeHandlerRegistry.getFileTypeHandlers().stream()
                .map(fileTypeHandler -> {
                    return new FileChooser.ExtensionFilter(fileTypeHandler.getName(), fileTypeHandler.getSupportedExtensions().stream().map(ext -> "*." + ext).collect(Collectors.toList()));
                })
                .forEach(extensionFilter -> fileChooser.getExtensionFilters().add(extensionFilter));
        List<String> allSupportedFileExtensions = fileTypeHandlerRegistry.getFileTypeHandlers().stream().flatMap(fileTypeHandler -> fileTypeHandler.getSupportedExtensions().stream().map(ext -> "*." + ext))
                .collect(Collectors.toList());
        final FileChooser.ExtensionFilter defaultExtensionFilter = new FileChooser.ExtensionFilter(DEFAULT_FILE_EXTENSION_FILTER_NAME, allSupportedFileExtensions);
        fileChooser.getExtensionFilters().add(defaultExtensionFilter);
        fileChooser.setSelectedExtensionFilter(defaultExtensionFilter);
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

    @Reference
    public void setRecentFilesRegistry(RecentFilesRegistry recentFilesRegistry) {
        this.recentFilesRegistry = recentFilesRegistry;
    }

}
