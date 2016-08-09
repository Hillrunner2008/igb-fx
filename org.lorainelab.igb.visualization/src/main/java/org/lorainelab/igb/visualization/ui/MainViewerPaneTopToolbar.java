package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = MainViewerPaneTopToolbar.class)
public class MainViewerPaneTopToolbar extends HBox {

    private SearchBox searchBox;
    private LoadDataButton loadDataButton;
    private LoadSequenceButton loadSequenceButton;
    private HorizontalZoomSlider horizontalZoomSlider;

    public MainViewerPaneTopToolbar() {
        VBox.setVgrow(this, Priority.NEVER);
//        VBox.setMargin(this, Insets.EMPTY);
    }

    @Activate
    public void activate() {
        HBox.setMargin(searchBox, new Insets(5));
        horizontalZoomSlider.setPadding(new Insets(10, 10, 5, 10));
        HBox.setMargin(loadDataButton, new Insets(5, 5, 0, 0));
        HBox.setMargin(loadSequenceButton, new Insets(5, 5, 0, 0));
        Platform.runLater(() -> {
            getChildren().add(searchBox);
            getChildren().add(horizontalZoomSlider);
            getChildren().add(loadDataButton);
            getChildren().add(loadSequenceButton);
        });
    }

    @Reference
    public void setSearchBox(SearchBox searchBox) {
        this.searchBox = searchBox;
    }

    @Reference
    public void setLoadDataButton(LoadDataButton loadDataButton) {
        this.loadDataButton = loadDataButton;
    }

    @Reference
    public void setLoadSequenceButton(LoadSequenceButton loadSequenceButton) {
        this.loadSequenceButton = loadSequenceButton;
    }

    @Reference
    public void setHorizontalZoomSlider(HorizontalZoomSlider horizontalZoomSlider) {
        this.horizontalZoomSlider = horizontalZoomSlider;
    }

}
