package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.scene.control.Button;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = LoadDataButton.class)
public class LoadDataButton extends Button {

    private static final Logger LOG = LoggerFactory.getLogger(LoadDataButton.class);
    private SelectionInfoService selectionInfoService;
    private CanvasModel canvasModel;

    public LoadDataButton() {
        setText("Load Data");
    }

    @Activate
    public void activate() {
        setOnAction(action -> {
            selectionInfoService.getSelectedGenomeVersion().get().ifPresent(selectedGenomeVersion -> {
                selectionInfoService.getSelectedChromosome().get().ifPresent(selectedChromosome -> {
                    selectedGenomeVersion.getLoadedDataSets().forEach(dataSet -> {
                        CompletableFuture.supplyAsync(() -> {
                            dataSet.loadRegion(selectedChromosome, canvasModel.getCurrentModelCoordinatesInView());
                            return null;
                        }).thenRun(() -> {
                            Platform.runLater(() -> {
                                canvasModel.forceRefresh();
                            });
                        });
                    });
                });
            });
        });
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    @Reference(unbind = "removeSelectionInfoService")
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    public void removeSelectionInfoService(SelectionInfoService selectionInfoService) {
        LOG.info("removeSelectionInfoService called");
    }

}
