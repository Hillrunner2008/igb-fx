/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.loadurl;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.miginfocom.layout.CC;
import org.lorainelab.igb.datasetloadingservice.api.DataSetLoadingService;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.tbee.javafx.scene.layout.MigPane;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true)
public class LoadUrlMenuItem implements MenuBarEntryProvider {

    private WeightedMenuItem menuItem;
    private DataSetLoadingService datasetOpener;
    private SelectionInfoService selectionInfoService;

    MigPane migPane;
    Stage stage;
    private Button okBtn;
    private Button cancelBtn;
    private Label messageLabel;
    private TextField urlTextField;

    public LoadUrlMenuItem() {
        menuItem = new WeightedMenuItem(5, "Load URL");
    }

    @Activate
    public void activate() {
        Platform.runLater(() -> {
            initComponents();
            layoutComponents();
            menuItem.setOnAction(event -> {
                Platform.runLater(() -> {
                    stage.centerOnScreen();
                    stage.show();
                });
            });

        });
    }

    private void initComponents() {
        menuItem.setDisable(!selectionInfoService.getSelectedGenomeVersion().get().isPresent());
        selectionInfoService.getSelectedGenomeVersion().addListener((observable, oldValue, newValue) -> {
            menuItem.setDisable(!selectionInfoService.getSelectedGenomeVersion().get().isPresent());
        });
        migPane = new MigPane("fillx", "[]rel[grow]", "[][][]");
        stage = new Stage();
        stage.setMinWidth(300);
        stage.initModality(Modality.APPLICATION_MODAL);
        okBtn = new Button("Okay");
        cancelBtn = new Button("Cancel");
        messageLabel = new Label("Enter URL to open");
        urlTextField = new TextField();
        urlTextField.setMinWidth(250);
        okBtn.setOnAction(ae -> {
            String text = urlTextField.getText();
            if (!Strings.isNullOrEmpty(text)) {
                datasetOpener.openHttpDataSet(text);
            }
            stage.hide();
        });
        cancelBtn.setOnAction(ae -> stage.hide());

    }

    private void layoutComponents() {
        migPane.add(messageLabel, "wrap");
        migPane.add(urlTextField, "growx, wrap");
        migPane.add(okBtn, new CC().gap("rel").x("container.x+50").span(3).tag("ok").split());
        migPane.add(cancelBtn, new CC().x("container.x+150").tag("ok"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(true);
        Scene scene = new Scene(migPane);
        stage.setScene(scene);
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems() {
        final List<WeightedMenuEntry> menuItems = Lists.newArrayList(menuItem);
        return Optional.of(menuItems);
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.FILE;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference
    public void setFileOpener(DataSetLoadingService fileOpener) {
        this.datasetOpener = fileOpener;
    }

}
