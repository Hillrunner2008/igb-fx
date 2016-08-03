package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.scene.control.Button;
import org.lorainelab.igb.selections.SelectionInfoService;
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

    public LoadDataButton() {
        setText("Load Data");
    }

    @Activate
    public void activate() {
//        setOnAction(action -> {
//            selectionInfoService.getSelectedGenomeVersion().get().ifPresent(selectedGenomeVersion -> {
//                selectionInfoService.getSelectedChromosome().get().ifPresent(selectedChromosome -> {
//                    selectedGenomeVersion.getLoadedDataSets().forEach(dataSet -> {
//                        CompletableFuture.supplyAsync(() -> {
//                            dataSet.loadRegion(selectedChromosome.getName(), getCurrentRange());
//                            return null;
//                        }).thenRun(() -> {
//                            Platform.runLater(() -> {
//                                //refresh
//                            });
//                        });
//                    });
//                });
//            });
//        });
    }

    @Reference(unbind = "removeSelectionInfoService")
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    public void removeSelectionInfoService(SelectionInfoService selectionInfoService) {
        LOG.info("removeSelectionInfoService called");
    }
}
