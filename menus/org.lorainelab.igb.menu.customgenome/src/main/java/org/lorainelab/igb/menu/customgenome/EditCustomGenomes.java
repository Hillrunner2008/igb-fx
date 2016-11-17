package org.lorainelab.igb.menu.customgenome;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.miginfocom.layout.CC;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.data.model.sequence.ReferenceSequenceProvider;
import org.lorainelab.igb.data.model.util.TwoBitParser;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.preferences.SessionPreferences;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.synonymservice.ChromosomeSynomymService;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true)
public class EditCustomGenomes implements MenuBarEntryProvider {

    private static final Logger LOG = LoggerFactory.getLogger(EditCustomGenomes.class);
    private WeightedMenuItem menuItem;
    private Stage stage;

    @FXML
    private AnchorPane anchroPane;
    @FXML
    private TableView genomesTable;
    @FXML
    private TableColumn speciesColumn;
    @FXML
    private TableColumn versionColumn;
    @FXML
    private TableColumn filePathCoulmn;
    @FXML
    private Button openButton;
    @FXML
    private Button editButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button deleteButton;

    private Stage editStage;
    private Button editCancelBtn;
    private MigPane editMigPane;
    private Button refSeqBrowseBtn;
    private String recentFilePath = null;
    private Button okBtn;
    //private Button cancelBtn;
    private Label refSeqLabel;
    private TextField refSeqTextField;
    private Label speciesLabel;
    private TextField speciesTextField;
    private Label versionLabel;
    private TextField versionTextField;
    private GenomeVersion genomeToEdit = null;

    private SelectionInfoService selectionInfoService;
    private CustomGenomePersistenceManager customGenomePersistenceManager;
    private GenomeVersionRegistry genomeVersionRegistry;
    private ChromosomeSynomymService chromosomeSynomymService;
    private ObservableList<GenomeVersion> genomeVersionList;

    public EditCustomGenomes() {
        menuItem = new WeightedMenuItem(2, "My Genomes");
        final URL resource = EditCustomGenomes.class.getClassLoader().getResource("MyGenomes.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                fxmlLoader.load();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
//        genomeVersionList = FXCollections.observableArrayList();
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems() {
        final List<WeightedMenuEntry> menuItems = Lists.newArrayList(menuItem);
        return Optional.of(menuItems);
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.GENOME;
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

        genomeVersionList = FXCollections.observableArrayList(genomeVersionRegistry.getRegisteredGenomeVersions());
        genomeVersionRegistry.getRegisteredGenomeVersions().addListener(new SetChangeListener<GenomeVersion>() {
            @Override
            public void onChanged(SetChangeListener.Change<? extends GenomeVersion> change) {
                Platform.runLater(() -> {
                    if (change.wasAdded()) {
                        if (!genomeVersionList.contains(change.getElementAdded())) {
                            final GenomeVersion elementAdded = change.getElementAdded();
                            genomeVersionList.add(elementAdded);
                        }
                    }
                    if (change.wasRemoved()) {
                        final GenomeVersion elementRemoved = change.getElementRemoved();
                        genomeVersionList.remove(elementRemoved);
                    }
                });
            }
        });

        openButton.setOnAction((ActionEvent event) -> {
            ObservableList selectedItems = genomesTable.getSelectionModel().getSelectedItems();
            if (selectedItems.size() != 0) {
                genomeVersionRegistry.setSelectedGenomeVersion((GenomeVersion) selectedItems.get(0));
//                stage.hide();
            }
        });

        cancelButton.setOnAction((ActionEvent event) -> stage.hide());

        deleteButton.setOnAction((ActionEvent event) -> {
            ObservableList selectedItems = genomesTable.getSelectionModel().getSelectedItems();
            if (selectedItems.size() != 0) {
                //confirmation?
                customGenomePersistenceManager.deleteCustomGenome((GenomeVersion) selectedItems.get(0));
//                stage.hide();
            }
        });

        editButton.setOnAction(ae -> {
            ObservableList selectedItems = genomesTable.getSelectionModel().getSelectedItems();
            if (selectedItems.size() == 0) {
                return;
//                stage.hide();
            }
            GenomeVersion genome = (GenomeVersion) selectedItems.get(0);
            speciesTextField.setText(genome.getSpeciesName().get());
            versionTextField.setText(genome.name().get());
            refSeqTextField.setText(genome.getReferenceSequenceProvider().getPath());
            genomeToEdit = genome;
            Platform.runLater(() -> editStage.show());
        });

        genomesTable.setEditable(true);
        genomesTable.setItems(genomeVersionList);//FXCollections.observableArrayList(genomeVersions));

        speciesColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
                return param.getValue().getSpeciesName();
            }
        });
        versionColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
                return param.getValue().name();
            }
        });
        filePathCoulmn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
                String[] path = param.getValue().getReferenceSequenceProvider().getPath().split(File.separator);
                return new SimpleStringProperty(path[path.length - 1]);
            }
        });
        cancelButton.setOnAction(event -> {
            stage.hide();
        });

        stage = new Stage();
        stage.setResizable(true);
        stage.setTitle("My Genomes");

        //init edit stage
        editMigPane = new MigPane("fillx", "[]rel[grow]", "[][][]");
        editStage = new Stage();
        editStage.setTitle("Edit custom genome");
        editStage.setMinWidth(440);
        editStage.setMinHeight(165);
        editStage.setHeight(165);
        editStage.setMaxHeight(165);
        editCancelBtn = new Button("Exit");
        editCancelBtn.setOnAction(event -> {
            editStage.hide();
        });

        speciesLabel = new Label("Species");
        speciesTextField = new TextField();
        versionLabel = new Label("Genome Version");
        versionTextField = new TextField();
        refSeqLabel = new Label("Reference Sequence");
        refSeqTextField = new TextField();
        refSeqTextField.setEditable(false);
        refSeqBrowseBtn = new Button("Choose File\u2026");
//        refSeqBrowseBtn.setDisable(true);
        refSeqBrowseBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Sequence File");
            File homeDirectory;
            if (SessionPreferences.getRecentSelectedFilePath() != null) {
                File file = new File(SessionPreferences.getRecentSelectedFilePath());
                String simpleFileName = file.getParent();
                homeDirectory = new File(simpleFileName);
            } else {
                homeDirectory = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(homeDirectory);
            addFileExtensionFilters(fileChooser);
            Optional.ofNullable(fileChooser.showOpenDialog(null)).ifPresent(selectedFile -> {
                try {
                    refSeqTextField.setText(selectedFile.getCanonicalPath());
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            });

        });

        refSeqTextField.setOnKeyPressed(this::handleKeyEvent);
        speciesTextField.setOnKeyPressed(this::handleKeyEvent);
        versionTextField.setOnKeyPressed(this::handleKeyEvent);

        okBtn = new Button("Ok");
        okBtn.setOnAction(event -> {
            boolean[] customGenomeEdited = {false};
            CompletableFuture.runAsync(() -> {
                try {
                    final String speciesName = speciesTextField.textProperty().get();
                    final String versionName = versionTextField.textProperty().get();
                    final String sequenceFileUrl = refSeqTextField.textProperty().get();
                    if (!Strings.isNullOrEmpty(speciesName)
                            || !Strings.isNullOrEmpty(versionName)
                            || !Strings.isNullOrEmpty(sequenceFileUrl)) {

                        //if file path not changed simply update
                        if (genomeToEdit.getReferenceSequenceProvider().getPath().equals(sequenceFileUrl)) {
                            genomeToEdit.setName(versionName);
                            genomeToEdit.setSpeciesName(speciesName);
                            customGenomePersistenceManager.persistCustomGenome(genomeToEdit);
                            customGenomeEdited[0] = true;
                        } else {
                            //path changed to drop current genomne and re-create
                            Optional<GenomeVersion> duplicate = genomeVersionRegistry.getRegisteredGenomeVersions().stream()
                                    .filter(gv -> gv.getReferenceSequenceProvider().getPath().equalsIgnoreCase(sequenceFileUrl))
                                    .findFirst();
                            //check for duplicate with new file path
                            if (duplicate.isPresent()) {
                                Platform.runLater(() -> {
//                                    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.OK_DONE);
                                    Alert dlg = new Alert(Alert.AlertType.WARNING, "This sequence file is already mapped to the \n\""
                                            + duplicate.get().name().get() + "\" genome.");
                                    dlg.initModality(stage.getModality());
                                    dlg.initOwner(stage.getOwner());
                                    dlg.setTitle("Cannot add duplicate genome version");
//                                    dlg.getButtonTypes().setAll(cancelBtn);
                                    Optional<ButtonType> result = dlg.showAndWait();
                                });
                            } else {
                                //delete and add new
                                customGenomePersistenceManager.deleteCustomGenome(genomeToEdit);
                                ReferenceSequenceProvider twoBitProvider = (ReferenceSequenceProvider) new TwoBitParser(sequenceFileUrl, chromosomeSynomymService);
                                GenomeVersion customGenome = new GenomeVersion(versionName, speciesName, twoBitProvider, versionName);
                                customGenomePersistenceManager.persistCustomGenome(customGenome);
                                SessionPreferences.setRecentSelectedFilePath(sequenceFileUrl);
                                customGenomeEdited[0] = genomeVersionRegistry.getRegisteredGenomeVersions().add(customGenome);
                                genomeVersionRegistry.setSelectedGenomeVersion(customGenome);

                            }
                        }
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }).whenComplete((result, ex) -> {
                //TODO show error in popup instead of hiding if it occurred
                Platform.runLater(() -> {
                    if (customGenomeEdited[0]) {
                        customGenomeEdited[0] = false;
                        Alert dlg = new Alert(Alert.AlertType.INFORMATION, "Custom Genome edited");
                        dlg.setWidth(600);
                        dlg.initModality(editStage.getModality());
                        dlg.initOwner(editStage.getOwner());
                        dlg.setTitle("Genome Edited Successfully");
                        dlg.show();
                        dlg.setOnCloseRequest(closeEvent -> {
                            editStage.hide();
                        });
                    } else {
                        editStage.hide();
                    }
                });
            });

        });
    }

    private void handleKeyEvent(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
                okBtn.fire();
                break;
            case ESCAPE:
                cancelButton.fire();
                break;
            case K:
                if (event.isControlDown()) {
                    ((TextField) event.getSource()).deleteText(((TextField) event.getSource()).getCaretPosition(), ((TextField) event.getSource()).getLength());
                }
        }
    }

    private void layoutComponents() {
        Scene scene = new Scene(anchroPane);
        stage.setScene(scene);
        stage.sizeToScene();

        editMigPane.add(refSeqLabel);
        editMigPane.add(refSeqTextField, "growx");
        editMigPane.add(refSeqBrowseBtn, "wrap");
        editMigPane.add(speciesLabel, "");
        editMigPane.add(speciesTextField, "growx, wrap");
        editMigPane.add(versionLabel, "");
        editMigPane.add(versionTextField, "growx, wrap");
        editMigPane.add(okBtn, new CC().gap("rel").x("container.x+150").span(3).tag("ok").split());
        editMigPane.add(editCancelBtn, new CC().x("container.x+200").tag("ok"));
        editStage.initModality(Modality.APPLICATION_MODAL);
        editStage.setResizable(true);
        Scene editScene = new Scene(editMigPane);
        editStage.setScene(editScene);
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }

    @Reference
    public void setCustomGenomePersistenceManager(CustomGenomePersistenceManager customGenomePersistenceManager) {
        this.customGenomePersistenceManager = customGenomePersistenceManager;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference
    public void setChromosomeSynomymService(ChromosomeSynomymService chromosomeSynomymService) {
        this.chromosomeSynomymService = chromosomeSynomymService;
    }

    private void addFileExtensionFilters(FileChooser fileChooser) {
        //TODO setup dynamic registry for ReferenceSequenceProvider ... will require ReferenceSequenceProvider to hold supported extensions
        FileChooser.ExtensionFilter twoBitExtensionFilter = new FileChooser.ExtensionFilter("2bit", Lists.newArrayList("*.2bit"));
        fileChooser.getExtensionFilters().add(twoBitExtensionFilter);
    }

}

//package org.lorainelab.igb.menu.customgenome;
//
//import aQute.bnd.annotation.component.Activate;
//import aQute.bnd.annotation.component.Component;
//import aQute.bnd.annotation.component.Reference;
//import com.google.common.base.Strings;
//import com.google.common.collect.Lists;
//import java.io.File;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.CompletableFuture;
//import javafx.application.Platform;
//import javafx.beans.property.SimpleStringProperty;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.collections.SetChangeListener;
//import javafx.scene.Scene;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.TableColumn;
//import javafx.scene.control.TablePosition;
//import javafx.scene.control.TableView;
//import javafx.scene.control.TextField;
//import javafx.stage.FileChooser;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//import javafx.util.Callback;
//import net.miginfocom.layout.CC;
//import org.lorainelab.igb.data.model.GenomeVersion;
//import org.lorainelab.igb.data.model.GenomeVersionRegistry;
//import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
//import org.lorainelab.igb.menu.api.model.ParentMenu;
//import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
//import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
//import org.lorainelab.igb.selections.SelectionInfoService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.tbee.javafx.scene.layout.MigPane;
//
///**
// *
// * @author Devdatta Kulkarni
// */
//@Component(immediate = true)
//public class EditCustomGenomes implements MenuBarEntryProvider {
//
//    private static final Logger LOG = LoggerFactory.getLogger(EditCustomGenomes.class);
//    private WeightedMenuItem menuItem;
//    private Stage stage;
//    private Button cancelBtn;
//    private MigPane migPane;
//    private TableView table;
//    private TableColumn species;
//    private TableColumn version;
//    private TableColumn fileName;
//    private TableColumn editColumn;
//    private TableColumn deleteColumn;
//    private SelectionInfoService selectionInfoService;
//    private CustomGenomePersistenceManager customGenomePersistenceManager;
//    private GenomeVersionRegistry genomeVersionRegistry;
//    private ObservableList<GenomeVersion> genomeVersionList;
//
//    private Stage editStage;
//    private Button editCancelBtn;
//    private MigPane editMigPane;
//    private Button refSeqBrowseBtn;
//    private String recentFilePath = null;
//    private Button okBtn;
//    //private Button cancelBtn;
//    private Label refSeqLabel;
//    private TextField refSeqTextField;
//    private Label speciesLabel;
//    private TextField speciesTextField;
//    private Label versionLabel;
//    private TextField versionTextField;
//    private GenomeVersion genomeToEdit = null;
//
//    public EditCustomGenomes() {
//        menuItem = new WeightedMenuItem(2, "Manage Custom Genomes");
//    }
//
//    @Override
//    public Optional<List<WeightedMenuEntry>> getMenuItems() {
//        final List<WeightedMenuEntry> menuItems = Lists.newArrayList(menuItem);
//        return Optional.of(menuItems);
//    }
////    @Override
////    public Optional<List<WeightedMenuEntry>> getMenuItems() {
////        return Optional.ofNullable(Lists.newArrayList(menuItem));
//////        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
////    }
//
//    @Override
//    public ParentMenu getParentMenu() {
//        return ParentMenu.GENOME;
//    }
//
//    @Activate
//    public void activate() {
//        Platform.runLater(() -> {
//            initComponents();
//            layoutComponents();
//            menuItem.setOnAction(event -> {
//                Platform.runLater(() -> {
//                    stage.centerOnScreen();
//                    stage.show();
//                });
//            });
//        });
//
//    }
//
//    private void initComponents() {
//
//        genomeVersionList = FXCollections.observableArrayList(genomeVersionRegistry.getRegisteredGenomeVersions());
//        //Convert to lambda ??
//        genomeVersionRegistry.getRegisteredGenomeVersions().addListener(new SetChangeListener<GenomeVersion>() {
//            @Override
//            public void onChanged(SetChangeListener.Change<? extends GenomeVersion> change) {
//                if (change.wasAdded()) {
//                    genomeVersionList.add(change.getElementAdded());
//                }
//                if (change.wasRemoved()) {
//                    genomeVersionList.remove(change.getElementRemoved());
//                }
//            }
//        });
//
//        editMigPane = new MigPane("fillx", "[]rel[grow]", "[][][]");
//        editStage = new Stage();
//        editStage.setTitle("Edit custom genome");
//        editStage.setMinWidth(440);
//        editStage.setMinHeight(165);
//        editStage.setHeight(165);
//        editStage.setMaxHeight(165);
//        editCancelBtn = new Button("Exit");
//        editCancelBtn.setOnAction(event -> {
//            editStage.hide();
//        });
//
//        speciesLabel = new Label("Species");
//        speciesTextField = new TextField();
//        versionLabel = new Label("Genome Version");
//        versionTextField = new TextField();
//        refSeqLabel = new Label("Reference Sequence");
//        refSeqTextField = new TextField();
//        refSeqTextField.setEditable(false);
//        refSeqBrowseBtn = new Button("Choose File\u2026");
//        refSeqBrowseBtn.setDisable(true);
////        refSeqBrowseBtn.setOnAction(event -> {
////            FileChooser fileChooser = new FileChooser();
////            fileChooser.setTitle("Choose Sequence File");
////            File homeDirectory;
////            if (SessionPreferences.getRecentSelectedFilePath() != null) {
////                File file = new File(SessionPreferences.getRecentSelectedFilePath());
////                String simpleFileName = file.getParent();
////                homeDirectory = new File(simpleFileName);
////            } else {
////                homeDirectory = new File(System.getProperty("user.home"));
////            }
////            fileChooser.setInitialDirectory(homeDirectory);
////            addFileExtensionFilters(fileChooser);
////            Optional.ofNullable(fileChooser.showOpenDialog(null)).ifPresent(selectedFile -> {
////                try {
////                    refSeqTextField.setText(selectedFile.getCanonicalPath());
////                } catch (Exception ex) {
////                    LOG.error(ex.getMessage(), ex);
////                }
////            });
////
////        });
//
//        okBtn = new Button("Ok");
//        okBtn.setOnAction(event -> {
//            boolean customGenomeEdited = true;
//            CompletableFuture.runAsync(() -> {
//                try {
//                    final String speciesName = speciesTextField.textProperty().get();
//                    final String versionName = versionTextField.textProperty().get();
//                    if (!Strings.isNullOrEmpty(speciesName)
//                            || !Strings.isNullOrEmpty(versionName)) {
//                        //Value change of item stored in list will not trigger any event.. need to delete and reinsert to trigger event.
//                        genomeToEdit.setName(versionName);
//                        genomeToEdit.setSpeciesName(speciesName);
//                        customGenomePersistenceManager.persistCustomGenome(genomeToEdit);
//                        int index = table.getItems().indexOf(genomeToEdit);
//                        table.getItems().remove(genomeToEdit);
//                        table.getItems().add(index, genomeToEdit);
//                    }
//                } catch (Exception ex) {
//                    LOG.error(ex.getMessage(), ex);
//                }
//            }).whenComplete((result, ex) -> {
//                //TODO show error in popup instead of hiding if it occurred
//                Platform.runLater(() -> {
//                    if (customGenomeEdited) {
//                        Alert dlg = new Alert(Alert.AlertType.INFORMATION, "Custom Genome edited");
//                        dlg.setWidth(600);
//                        dlg.initModality(editStage.getModality());
//                        dlg.initOwner(editStage.getOwner());
//                        dlg.setTitle("Genome Edited Successfully");
//                        dlg.show();
//                        dlg.setOnCloseRequest(closeEvent -> {
//                            editStage.hide();
//                        });
//                    } else {
//                        editStage.hide();
//                    }
//                });
//            });
//
//        });
//
//        migPane = new MigPane("fillx", "[]rel[grow]", "[][][]");
//        table = new TableView();
//        table.setEditable(true);
//
//        species = new TableColumn("Species");
//        version = new TableColumn("Version");
//        fileName = new TableColumn("File Name");
//        editColumn = new TableColumn("Settings");
//        deleteColumn = new TableColumn("Delete");
//        table.getColumns().addAll(species, version, fileName, editColumn, deleteColumn);
//        table.setItems(genomeVersionList);//FXCollections.observableArrayList(genomeVersions));
//        species.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
//            @Override
//            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
//                return param.getValue().getSpeciesName();
//            }
//        });
//        version.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
//            @Override
//            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
//                return param.getValue().getName();
//            }
//        });
//        fileName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
//            @Override
//            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
//                String[] path = param.getValue().getReferenceSequenceProvider().getPath().split(File.separator);
//                return new SimpleStringProperty(path[path.length - 1]);
//            }
//        });
//        cancelBtn = new Button("Exit");
//        cancelBtn.setOnAction(event -> {
//            stage.hide();
//        });
//        table.setOnMouseClicked(event -> {
//            TablePosition focusedCell = table.getFocusModel().getFocusedCell();
//            //delete genome
//            if (focusedCell.getColumn() == 4) {
//                customGenomePersistenceManager.deleteCustomGenome(genomeVersionList.get(focusedCell.getRow()));
//                genomeVersionRegistry.getRegisteredGenomeVersions().remove(genomeVersionList.get(focusedCell.getRow()));
//            } //edit genome
//            else if (focusedCell.getColumn() == 3) {
//                GenomeVersion genome = genomeVersionList.get(focusedCell.getRow());
//                speciesTextField.setText(genome.getSpeciesName().get());
//                versionTextField.setText(genome.getName().get());
//                refSeqTextField.setText(genome.getReferenceSequenceProvider().getPath());
//                genomeToEdit = genome;
//                Platform.runLater(() -> editStage.show());
//            }
//            Platform.runLater(() -> {
//                table.requestFocus();
//                if (!table.getSelectionModel().isEmpty()) {
//                    table.getFocusModel().focus(0);
//                }
//            });
//
//        });
//
//        stage = new Stage();
//        table.setMinWidth(650);
//        stage.setWidth(table.getWidth() + 15);
//        table.getColumns().forEach(col -> ((TableColumn) col).widthProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                stage.setWidth(table.getColumns().stream().mapToDouble(col -> ((TableColumn) col).getWidth()).sum() + 15);
//                migPane.setMinWidth(stage.getWidth());
//                table.setMinWidth(table.getColumns().stream().mapToDouble(col -> ((TableColumn) col).getWidth()).sum());
//            }
//        }));
//        stage.setTitle("Edit or delete custom genome");
//    }
//
//    private void layoutComponents() {
//        migPane.add(table, "wrap");
//        migPane.add(cancelBtn, new CC().gap("rel").tag("ok").span(3).split());
//        stage.setResizable(false);
//        Scene scene = new Scene(migPane);
//        stage.setScene(scene);
//        stage.sizeToScene();
//        editMigPane.add(refSeqLabel);
//        editMigPane.add(refSeqTextField, "growx");
//        editMigPane.add(refSeqBrowseBtn, "wrap");
//        editMigPane.add(speciesLabel, "");
//        editMigPane.add(speciesTextField, "growx, wrap");
//        editMigPane.add(versionLabel, "");
//        editMigPane.add(versionTextField, "growx, wrap");
//        editMigPane.add(okBtn, new CC().gap("rel").x("container.x+150").span(3).tag("ok").split());
//        editMigPane.add(editCancelBtn, new CC().x("container.x+200").tag("ok"));
//        editStage.initModality(Modality.APPLICATION_MODAL);
//        editStage.setResizable(true);
//        Scene editScene = new Scene(editMigPane);
//
//        editStage.setScene(editScene);
//
//    }
//
//    @Reference
//    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
//        this.genomeVersionRegistry = genomeVersionRegistry;
//    }
//
//    @Reference
//    public void setCustomGenomePersistenceManager(CustomGenomePersistenceManager customGenomePersistenceManager) {
//        this.customGenomePersistenceManager = customGenomePersistenceManager;
//    }
//
//    @Reference
//    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
//        this.selectionInfoService = selectionInfoService;
//    }
//
//    private void addFileExtensionFilters(FileChooser fileChooser) {
//        //TODO setup dynamic registry for ReferenceSequenceProvider ... will require ReferenceSequenceProvider to hold supported extensions
//        FileChooser.ExtensionFilter twoBitExtensionFilter = new FileChooser.ExtensionFilter("2bit", Lists.newArrayList("*.2bit"));
//        fileChooser.getExtensionFilters().add(twoBitExtensionFilter);
//    }
//
//}
