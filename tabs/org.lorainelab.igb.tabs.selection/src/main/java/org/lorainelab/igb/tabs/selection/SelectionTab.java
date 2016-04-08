package org.lorainelab.igb.tabs.selection;

import aQute.bnd.annotation.component.Component;
import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import org.lorainelab.igb.visualization.tabs.api.TabDockingPosition;
import org.lorainelab.igb.visualization.tabs.api.TabProvider;

@Component(immediate = true)
public class SelectionTab implements TabProvider {

    private static final String TAB_TITLE = "Selection Info";
    private final int TAB_WEIGHT = 0;
    private final Tab selectionTab;
    @FXML
    private AnchorPane tabContent;

    public SelectionTab() {
        selectionTab = new Tab(TAB_TITLE);
        final URL resource = SelectionTab.class.getClassLoader().getResource("SelectionTab.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        Platform.runLater(() -> {
            try {
                fxmlLoader.load();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @FXML
    private void initialize() {
        selectionTab.setContent(tabContent);
    }

    @Override
    public Tab getTab() {
        return selectionTab;
    }

    @Override
    public TabDockingPosition getTabDockingPosition() {
        return TabDockingPosition.BOTTOM;
    }

    @Override
    public int getTabWeight() {
        return TAB_WEIGHT;
    }

}
