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
@Component(immediate = true, provide = LoadSequenceButton.class)
public class LoadSequenceButton extends Button {

    private static final Logger LOG = LoggerFactory.getLogger(LoadSequenceButton.class);
    private CanvasModel canvasModel;
    private SelectionInfoService selectionInfoService;

    public LoadSequenceButton() {
        setText("Load Sequence");
    }

    @Activate
    public void activate() {
        setOnAction(action -> {
            selectionInfoService.getSelectedChromosome().get().ifPresent(chr -> {
                CompletableFuture.supplyAsync(() -> {
                    chr.loadRegion(canvasModel.getCurrentModelCoordinatesInView());
                    return null;
                }).whenComplete((u, t) -> {
                    if (t != null) {
                        Throwable ex = (Throwable) t;
                        LOG.error(ex.getMessage(), ex);
                    }
                    Platform.runLater(() -> {
                        canvasModel.forceRefresh();
                    });
                });

            });

        });
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }
}
