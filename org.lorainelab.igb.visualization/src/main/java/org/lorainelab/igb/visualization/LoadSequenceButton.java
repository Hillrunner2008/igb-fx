package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Component;
import javafx.scene.control.Button;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = LoadSequenceButton.class)
public class LoadSequenceButton extends Button {

    public LoadSequenceButton() {
        setText("Load Sequence");
    }
//  final EventHandler<ActionEvent> loadSequenceActionListener = action -> {
//        Chromosome selectedChromosome = this.getState().getSelectedChromosome();
//        Optional.ofNullable(selectedChromosome).ifPresent(chr -> {
//            CompletableFuture.supplyAsync(() -> {
//                chr.loadRegion(getCurrentRange());
//                return null;
//            }).thenRun(() -> {
//                Platform.runLater(() -> {
//                    //TODO: hack for refresh
//                    AppStore.getStore().noop();
//                });
//            }).exceptionally(ex -> {
//                LOG.error(ex.getMessage(), ex);
//                return null;
//            });
//        });
//    };
}
