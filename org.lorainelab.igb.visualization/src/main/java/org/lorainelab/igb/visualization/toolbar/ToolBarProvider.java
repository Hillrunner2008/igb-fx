package org.lorainelab.igb.visualization.toolbar;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.time.Duration;
import javafx.collections.SetChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.reactfx.AwaitingEventStream;
import org.reactfx.EventStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = ToolBarProvider.class)
public class ToolBarProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ToolBarProvider.class);
    private ToolBar topToolbar;
    private Pane pane;
    private TextField selectionInfoTextField;

    private Button homeButton;
    private Button closeButton;
    private Button openFolderButton;
    private SelectionInfoService selectionInfoService;

    public ToolBarProvider() {
        topToolbar = new ToolBar();
        pane = new Pane();
        HBox.setHgrow(pane, Priority.ALWAYS);
        selectionInfoTextField = new TextField();
        selectionInfoTextField.setPrefWidth(400);
        selectionInfoTextField.setMaxWidth(400);
        selectionInfoTextField.setPromptText("Selection Info: Click the map to select annotation");
        openFolderButton = new Button("", new FontAwesomeIconView(FontAwesomeIcon.FOLDER_OPEN));
        openFolderButton.setOnAction(event -> {
            LOG.info("open file action");
        });
        closeButton = new Button("", new FontAwesomeIconView(FontAwesomeIcon.CLOSE));
        homeButton = new Button("", new FontAwesomeIconView(FontAwesomeIcon.HOME));
        topToolbar.getItems().addAll(
                openFolderButton,
                closeButton,
                homeButton,
                pane,
                selectionInfoTextField);
    }

    @Activate
    public void activate() {
        AwaitingEventStream<SetChangeListener.Change<? extends CompositionGlyph>> rebuildGridEventStream = EventStreams.changesOf(selectionInfoService.getSelectedGlyphs()).successionEnds(Duration.ofMillis(100));
        rebuildGridEventStream.subscribe(change -> updatedSelectionInfoText());
    }

    public ToolBar getTopToolbar() {
        return topToolbar;
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
