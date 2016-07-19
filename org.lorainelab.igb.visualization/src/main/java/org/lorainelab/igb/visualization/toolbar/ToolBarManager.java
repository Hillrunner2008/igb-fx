package org.lorainelab.igb.visualization.toolbar;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import java.time.Duration;
import java.util.Comparator;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.toolbar.api.ToolbarButtonProvider;
import org.lorainelab.igb.toolbar.api.WeightedButton;
import org.reactfx.AwaitingEventStream;
import org.reactfx.EventStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = ToolBarManager.class)
public class ToolBarManager {

    private static final Logger LOG = LoggerFactory.getLogger(ToolBarManager.class);
    private final Set<WeightedButton> toolbarEntries;
    private ToolBar topToolbar;
    private Pane pane;
    private TextField selectionInfoTextField;

    private SelectionInfoService selectionInfoService;

    public ToolBarManager() {
        toolbarEntries = Sets.newTreeSet(Comparator.comparingInt(button -> button.getWeight()));
        topToolbar = new ToolBar();
        pane = new Pane();
        HBox.setHgrow(pane, Priority.ALWAYS);
        selectionInfoTextField = new TextField();
        selectionInfoTextField.setPrefWidth(400);
        selectionInfoTextField.setMaxWidth(400);
        selectionInfoTextField.setPromptText("Selection Info: Click the map to select annotation");
        topToolbar.getItems().addAll(pane, selectionInfoTextField);
    }

    @Activate
    public void activate() {
        AwaitingEventStream<SetChangeListener.Change<? extends CompositionGlyph>> rebuildGridEventStream = EventStreams.changesOf(selectionInfoService.getSelectedGlyphs()).successionEnds(Duration.ofMillis(100));
        rebuildGridEventStream.subscribe(change -> updatedSelectionInfoText());
    }

    public ToolBar getTopToolbar() {
        return topToolbar;
    }

    @Reference(optional = true, multiple = true, unbind = "removeToolbarButtonProvider", dynamic = true)
    public void addToolbarButtonProvider(ToolbarButtonProvider buttonProvider) {
        Platform.runLater(() -> {
            topToolbar.getItems().clear();
            toolbarEntries.add(buttonProvider.getToolbarButton());
            toolbarEntries.forEach(entry -> topToolbar.getItems().add(entry));
            topToolbar.getItems().addAll(pane, selectionInfoTextField);
        });
    }

    public void removeToolbarButtonProvider(ToolbarButtonProvider buttonProvider) {
        toolbarEntries.remove(buttonProvider.getToolbarButton());
        topToolbar.getItems().remove(buttonProvider.getToolbarButton());
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    private void updatedSelectionInfoText() {
        if (selectionInfoService.getSelectedGlyphs().isEmpty()) {
            selectionInfoTextField.setText("");
        } else {
            selectionInfoTextField.setText("Selection Count: " + selectionInfoService.getSelectedGlyphs().size());
        }
    }

}
