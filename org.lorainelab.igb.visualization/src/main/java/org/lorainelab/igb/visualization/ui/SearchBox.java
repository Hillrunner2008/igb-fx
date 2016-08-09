package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.search.api.model.Document;
import org.lorainelab.igb.search.api.model.IndexIdentity;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = SearchBox.class)
public class SearchBox extends TextField {

    private static final Logger LOG = LoggerFactory.getLogger(SearchBox.class);
    private SearchService searchService;
    private ContextMenu searchAutocomplete;
    private SelectionInfoService selectionInfoService;

    public SearchBox() {
        searchAutocomplete = new ContextMenu();
        searchAutocomplete.hide();
        setPrefWidth(203.0);
        setPrefHeight(26.0);
    }
    
    @Activate
    public void activate() {
        initializeSearch();
    }

    private void initializeSearch() {
        setOnKeyReleased(e -> {
            Platform.runLater(() -> {
                if (getText().length() == 0) {
                    searchAutocomplete.hide();
                } else {
                    selectionInfoService.getSelectedGenomeVersion().get().ifPresent(selectedGenomeVersion -> {
                        selectionInfoService.getSelectedChromosome().get().ifPresent(selectedChromosome -> {
                            List<Document> searchResult = new LinkedList<>();
                            Optional<IndexIdentity> resourceIndexIdentity
                                    = searchService.getResourceIndexIdentity(
                                            selectedGenomeVersion.getSpeciesName());
                            if (resourceIndexIdentity.isPresent()) {
                                //TODO: refactor to boolean queries in search module
                                searchService.search("(chromosomeId:" + selectedChromosome.getName() + ") AND (id:" + getText() + "*)",
                                        resourceIndexIdentity.get()).stream()
                                        .forEach(doc -> searchResult.add(doc));
                            }
                            if (searchResult.size() > 0) {
                                populatePopup(searchResult);
                                if (!searchAutocomplete.isShowing()) {
                                    searchAutocomplete.show(this, Side.BOTTOM, 0, 0);
                                }
                            } else {
                                searchAutocomplete.hide();
                            }
                        });
                    });
                }
            });
        });
    }

    private void populatePopup(List<Document> searchResult) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        int maxEntries = 10;
        int count = Math.min(searchResult.size(), maxEntries);
        for (int i = 0; i < count; i++) {
            final Document result = searchResult.get(i);
            Label entryLabel = new Label(result.getFields().get("id"));
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            item.setOnAction(event -> {
                Platform.runLater(() -> {
                    setText(result.getFields().get("id"));
                    searchAutocomplete.hide();
                    int start = Integer.parseInt(result.getFields().get("start"));
                    int end = Integer.parseInt(result.getFields().get("end"));
//                    trackRenderers.stream().findFirst().ifPresent(trackRender -> {
//                        Rectangle2D oldRect = trackRender.getCanvasContext().getBoundingRect();
//                        Rectangle2D rect = new Rectangle2D(start, oldRect.getMinY(), end - start, oldRect.getHeight());
////                        eventBus.post(new JumpZoomEvent(rect, trackRender));
//                    });
                });

            });
            menuItems.add(item);
        }
        searchAutocomplete.getItems().clear();
        searchAutocomplete.getItems().addAll(menuItems);

    }

    @Reference(unbind = "removeSearchService")
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void removeSearchService(SearchService searchService) {
        LOG.info("removeSearchService called");
    }

    @Reference(unbind = "removeSelectionInfoService")
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    public void removeSelectionInfoService(SelectionInfoService selectionInfoService) {
        LOG.info("removeSelectionInfoService called");
    }
}
