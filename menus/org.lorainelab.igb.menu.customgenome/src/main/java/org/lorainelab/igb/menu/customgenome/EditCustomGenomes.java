package org.lorainelab.igb.menu.customgenome;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.miginfocom.layout.CC;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.selections.SelectionInfoService;
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
    private Button cancelBtn;
    private MigPane migPane;
    private TableView table;
    private TableColumn species;
    private TableColumn version;
    private TableColumn fileName;
    private TableColumn editColumn;
    private TableColumn deleteColumn;
    private SelectionInfoService selectionInfoService;
    private CustomGenomePersistenceManager customGenomePersistenceManager;
    private GenomeVersionRegistry genomeVersionRegistry;
    private ObservableList<GenomeVersion> genomeVersionList;

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

    public EditCustomGenomes() {
        menuItem = new WeightedMenuItem(2, "Manage Custom Genomes");
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems() {
        final List<WeightedMenuEntry> menuItems = Lists.newArrayList(menuItem);
        return Optional.of(menuItems);
    }
//    @Override
//    public Optional<List<WeightedMenuEntry>> getMenuItems() {
//        return Optional.ofNullable(Lists.newArrayList(menuItem));
////        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

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
        //Convert to lambda ?? 
        genomeVersionRegistry.getRegisteredGenomeVersions().addListener(new SetChangeListener<GenomeVersion>() {
            @Override
            public void onChanged(SetChangeListener.Change<? extends GenomeVersion> change) {
                if (change.wasAdded()) {
                    genomeVersionList.add(change.getElementAdded());
                }
                if (change.wasRemoved()) {
                    genomeVersionList.remove(change.getElementRemoved());
                }
            }
        });

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
        refSeqBrowseBtn.setDisable(true);
//        refSeqBrowseBtn.setOnAction(event -> {
//            FileChooser fileChooser = new FileChooser();
//            fileChooser.setTitle("Choose Sequence File");
//            File homeDirectory;
//            if (SessionPreferences.getRecentSelectedFilePath() != null) {
//                File file = new File(SessionPreferences.getRecentSelectedFilePath());
//                String simpleFileName = file.getParent();
//                homeDirectory = new File(simpleFileName);
//            } else {
//                homeDirectory = new File(System.getProperty("user.home"));
//            }
//            fileChooser.setInitialDirectory(homeDirectory);
//            addFileExtensionFilters(fileChooser);
//            Optional.ofNullable(fileChooser.showOpenDialog(null)).ifPresent(selectedFile -> {
//                try {
//                    refSeqTextField.setText(selectedFile.getCanonicalPath());
//                } catch (Exception ex) {
//                    LOG.error(ex.getMessage(), ex);
//                }
//            });
//
//        });

        okBtn = new Button("Ok");
        okBtn.setOnAction(event -> {
            boolean customGenomeEdited = true;
            CompletableFuture.runAsync(() -> {
                try {
                    final String speciesName = speciesTextField.textProperty().get();
                    final String versionName = versionTextField.textProperty().get();
                    if (!Strings.isNullOrEmpty(speciesName)
                            || !Strings.isNullOrEmpty(versionName)) {
                        //Value change of item stored in list will not trigger any event.. need to delete and reinsert to trigger event.
                        genomeToEdit.setName(versionName);
                        genomeToEdit.setSpeciesName(speciesName);
                        customGenomePersistenceManager.persistCustomGenome(genomeToEdit);
                        int index = table.getItems().indexOf(genomeToEdit);
                        table.getItems().remove(genomeToEdit);
                        table.getItems().add(index, genomeToEdit);
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }).whenComplete((result, ex) -> {
                //TODO show error in popup instead of hiding if it occurred
                Platform.runLater(() -> {
                    if (customGenomeEdited) {
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

        migPane = new MigPane("fillx", "[]rel[grow]", "[][][]");
        table = new TableView();
        table.setEditable(true);

        species = new TableColumn("Species");
        version = new TableColumn("Version");
        fileName = new TableColumn("File Name");
        editColumn = new TableColumn("Settings");
        deleteColumn = new TableColumn("Delete");
        table.getColumns().addAll(species, version, fileName, editColumn, deleteColumn);
        table.setItems(genomeVersionList);//FXCollections.observableArrayList(genomeVersions));
        species.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
                return param.getValue().getSpeciesName();
            }
        });
        version.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
                return param.getValue().name();
            }
        });
        fileName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
                String[] path = param.getValue().getReferenceSequenceProvider().getPath().split(File.separator);
                return new SimpleStringProperty(path[path.length - 1]);
            }
        });
        cancelBtn = new Button("Exit");
        cancelBtn.setOnAction(event -> {
            stage.hide();
        });
        table.setOnMouseClicked(event -> {
            TablePosition focusedCell = table.getFocusModel().getFocusedCell();
            //delete genome
            if (focusedCell.getColumn() == 4) {
                customGenomePersistenceManager.deleteCustomGenome(genomeVersionList.get(focusedCell.getRow()));
                genomeVersionRegistry.getRegisteredGenomeVersions().remove(genomeVersionList.get(focusedCell.getRow()));
            } //edit genome
            else if (focusedCell.getColumn() == 3) {
                GenomeVersion genome = genomeVersionList.get(focusedCell.getRow());
                speciesTextField.setText(genome.getSpeciesName().get());
                versionTextField.setText(genome.name().get());
                refSeqTextField.setText(genome.getReferenceSequenceProvider().getPath());
                genomeToEdit = genome;
                Platform.runLater(() -> editStage.show());
            }
            Platform.runLater(() -> {
                table.requestFocus();
                if (!table.getSelectionModel().isEmpty()) {
                    table.getFocusModel().focus(0);
                }
            });

        });

        stage = new Stage();
        table.setMinWidth(650);
        stage.setWidth(table.getWidth() + 15);
        table.getColumns().forEach(col -> ((TableColumn) col).widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                stage.setWidth(table.getColumns().stream().mapToDouble(col -> ((TableColumn) col).getWidth()).sum() + 15);
                migPane.setMinWidth(stage.getWidth());
                table.setMinWidth(table.getColumns().stream().mapToDouble(col -> ((TableColumn) col).getWidth()).sum());
            }
        }));
        stage.setTitle("Edit or delete custom genome");
    }

    private void layoutComponents() {
        migPane.add(table, "wrap");
        migPane.add(cancelBtn, new CC().gap("rel").tag("ok").span(3).split());
        stage.setResizable(false);
        Scene scene = new Scene(migPane);
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

    private void addFileExtensionFilters(FileChooser fileChooser) {
        //TODO setup dynamic registry for ReferenceSequenceProvider ... will require ReferenceSequenceProvider to hold supported extensions
        FileChooser.ExtensionFilter twoBitExtensionFilter = new FileChooser.ExtensionFilter("2bit", Lists.newArrayList("*.2bit"));
        fileChooser.getExtensionFilters().add(twoBitExtensionFilter);
    }

}
