/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.bookmarks.manager.impl;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lorainelab.igb.bookmarks.BookmarksTab;
import org.lorainelab.igb.bookmarks.data.Bookmark;
import org.lorainelab.igb.bookmarks.data.BookmarkData;
import org.lorainelab.igb.bookmarks.data.BookmarkFolder;
import org.lorainelab.igb.bookmarks.manager.BookmarkManager;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, provide = BookmarkManager.class)
public class BookmarkManagerImpl implements BookmarkManager {

    private Bookmark root;
    private GenomeVersionRegistry genomeVersionRegistry;

    @FXML
    private AnchorPane tabContent;
    @FXML
    private CheckBox defaultNmeCheckBox;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private RadioButton positionOnlyRadio;
    @FXML
    private RadioButton positionDataRadio;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    private ToggleGroup toggleGroup;

    private Stage stage;

    public BookmarkManagerImpl() {

        root = new BookmarkFolder("Bookmarks");
        root.setExpanded(true);

        final URL resource = BookmarksTab.class.getClassLoader().getResource("CreateBookmark.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                fxmlLoader.load();
                initComponents();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private void initComponents() {
        stage = new Stage();
        Scene scene = new Scene(tabContent);
        stage.setScene(scene);

        toggleGroup = new ToggleGroup();
        positionDataRadio.setToggleGroup(toggleGroup);
        positionOnlyRadio.setToggleGroup(toggleGroup);

        defaultNmeCheckBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                nameTextField.setDisable(!nameTextField.isDisable());
            }
        });

        cancelButton.setOnMouseClicked(ae -> stage.hide());
    }

    private void initBookmarkDefaults() {
        defaultNmeCheckBox.setSelected(true);
        positionDataRadio.setSelected(true);
        positionOnlyRadio.setSelected(false);
        nameTextField.setDisable(true);
        genomeVersionRegistry.getSelectedGenomeVersion().get().ifPresent(gv -> {
            nameTextField.setText(gv.getName().get());
            gv.getSelectedChromosomeProperty().get().ifPresent(chr -> {
                nameTextField.setText(nameTextField.getText() + " " + chr.getName());
            });
        });
        descriptionTextArea.setText("");
    }

    private void createTestBookmarks() {
        root.addChild(new BookmarkData("book1"));
        root.addChild(new BookmarkData("book2"));
        root.addChild(new BookmarkData("book3"));
        Bookmark folder2 = new BookmarkFolder("Folder2");
        folder2.addChild(new BookmarkData("book1"));
        folder2.addChild(new BookmarkData("book2"));
        folder2.addChild(new BookmarkData("book3"));
        root.addChild(folder2);
        Bookmark folder3 = new BookmarkFolder("Folder3");
        folder3.addChild(new BookmarkData("book1"));
        folder3.addChild(new BookmarkData("book2"));
        folder3.addChild(new BookmarkData("book3"));
        folder2.addChild(folder3);
    }

    @Override
    public Bookmark getRootBookmark() {
        return root;
    }

    @Override
    public void createBookmark(Bookmark refPosition) {
        initBookmarkDefaults();
        if (refPosition == null) {
            buildBookMark(root);
            return;
        } else if (refPosition.isLeaf()) {
            buildBookMark(refPosition.getParent());
        } else {
            buildBookMark(refPosition);
        }
    }

    @Override
    public void restoreBookmark(Bookmark b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createBookmark() {
        createBookmark(root);
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }

    private void buildBookMark(Bookmark parent) {

        if (!genomeVersionRegistry.getSelectedGenomeVersion().get().isPresent()) {
            Platform.runLater(() -> {
                Alert dlg = new Alert(Alert.AlertType.INFORMATION);
                dlg.setWidth(600);
                dlg.setTitle("Nothing to bookmark");
                dlg.setHeaderText("Nothing to bookmark");
                dlg.setContentText("No data available to bookmark");
                dlg.show();
            });
            return;
        }
        saveButton.setOnMouseClicked(ae -> {
            BookmarkData bookmarkData = new BookmarkData();
            bookmarkData.setName(nameTextField.getText());
            bookmarkData.setDescription(descriptionTextArea.getText());
            Map<String, String> data = new HashMap<String, String>();
            GenomeVersion genomeVersion;
            genomeVersionRegistry.getSelectedGenomeVersion().get().ifPresent(gv -> {
                data.put("version", gv.getName().get());
                data.put("species", gv.getSpeciesName().get());
                gv.getSelectedChromosomeProperty().get().ifPresent(chr -> {
                    data.put("chromosome", chr.getName());
                });
                data.put("refSeq", gv.getReferenceSequenceProvider().getPath());
            });

            bookmarkData.setDetails(data);

            bookmarkData.setParent(parent);
            parent.addChild(bookmarkData);

            stage.hide();
        });
        stage.show();

    }

}
