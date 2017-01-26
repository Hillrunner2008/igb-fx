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
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandlerRegistry;
import org.lorainelab.igb.datasetloadingservice.api.DataSetLoadingService;
import org.lorainelab.igb.preferences.SessionPreferences;
import org.lorainelab.igb.recentfiles.registry.api.RecentFilesRegistry;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true)
public class DataSetLoadingServiceImpl implements DataSetLoadingService {

    private static final Logger LOG = LoggerFactory.getLogger(DataSetLoadingServiceImpl.class);
    private static final String DEFAULT_FILE_EXTENSION_FILTER_NAME = "All Supported Formats";

    private FileTypeHandlerRegistry fileTypeHandlerRegistry;
    private SelectionInfoService selectionInfoService;
    private SearchService searchService;
    private RecentFilesRegistry recentFilesRegistry;

    @Override
    public void openDataSet() {
        openFileAction();
    }

    @Override
    public void openHttpDataSet(String path) {
        fileTypeHandlerRegistry.getFileTypeHandler(path).ifPresent(fileTypeHandler -> {
            selectionInfoService.getSelectedGenomeVersion().get().ifPresent(gv -> {
                gv.getLoadedDataSets().add(new DataSet(path, path, fileTypeHandler));
//                indexDataSetForSearch(fileTypeHandler, dataSourceReference);
            });
        });

    }

    @Override
    public void openDataSet(File file) {
        fileTypeHandlerRegistry.getFileTypeHandler(file.getPath()).ifPresent(fileTypeHandler -> {
            selectionInfoService.getSelectedGenomeVersion().get().ifPresent(gv -> {
                recentFilesRegistry.addRecentFile(file.getPath());
                gv.getLoadedDataSets().add(new DataSet(file.getName(), file.getPath(), fileTypeHandler));
//                indexDataSetForSearch(fileTypeHandler, dataSourceReference);
            });
        });
    }

    private void openFileAction() {
        FileChooser fileChooser = getFileChooser();
        Optional.ofNullable(fileChooser.showOpenMultipleDialog(null)).ifPresent(selectedFiles -> {
            selectedFiles.forEach(file -> {
                openDataSet(file);
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

//    private void indexDataSetForSearch(FileTypeHandler fileTypeHandler, DataSourceReference dataSourceReference) {
//        CompletableFuture<Void> indexTask = CompletableFuture.runAsync(() -> {
//            try {
//                Optional<GenomeVersion> genomeVersion = selectionInfoService.getSelectedGenomeVersion().getValue();
//                if (genomeVersion.isPresent()) {
//                    String speciesName = genomeVersion.get().getSpeciesName().get();
//                    Optional<IndexIdentity> resourceIndexIdentity = searchService.getResourceIndexIdentity(speciesName);
//                    if (!resourceIndexIdentity.isPresent()) {
//                        LOG.info("index doesnt exist, so create it");
//                        IndexIdentity indexIdentity = searchService.generateIndexIndentity();
//                        searchService.setResourceIndexIdentity(speciesName, indexIdentity);
//                        fileTypeHandler.createIndex(indexIdentity, dataSourceReference);
//                    } else {
//                        fileTypeHandler.createIndex(resourceIndexIdentity.get(), dataSourceReference);
//                    }
//                }
//            } catch (Exception ex) {
//                LOG.error(ex.getMessage(), ex);
//            }
//        }).whenComplete((result, ex) -> {
//            if (ex != null) {
//                LOG.error(ex.getMessage(), ex);
//            }
//        });
//    }
    private void addFileExtensionFilters(FileChooser fileChooser) {
        fileTypeHandlerRegistry.getFileTypeHandlers().stream()
                .map(fileTypeHandler -> {
                    return new FileChooser.ExtensionFilter(fileTypeHandler.getName(), fileTypeHandler.getSupportedExtensions().stream().map(ext -> "*." + ext).collect(Collectors.toList()));
                })
                .forEach(extensionFilter -> fileChooser.getExtensionFilters().add(extensionFilter));
        List<String> allSupportedFileExtensions = fileTypeHandlerRegistry.getAllSupportedFileExtensions().stream().map(ext -> "*." + ext).collect(Collectors.toList());
        final FileChooser.ExtensionFilter defaultExtensionFilter = new FileChooser.ExtensionFilter(DEFAULT_FILE_EXTENSION_FILTER_NAME, allSupportedFileExtensions);
        fileChooser.getExtensionFilters().add(defaultExtensionFilter);
        fileChooser.setSelectedExtensionFilter(defaultExtensionFilter);
    }

    @Reference(optional = false)
    public void setFileTypeHandlerRegistry(FileTypeHandlerRegistry fileTypeHandlerRegistry) {
        this.fileTypeHandlerRegistry = fileTypeHandlerRegistry;
    }

    @Reference(optional = false)
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference(optional = false)
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference(optional = false)
    public void setRecentFilesRegistry(RecentFilesRegistry recentFilesRegistry) {
        this.recentFilesRegistry = recentFilesRegistry;
    }

}
