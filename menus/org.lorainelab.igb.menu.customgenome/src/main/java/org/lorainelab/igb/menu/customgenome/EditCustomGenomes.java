package org.lorainelab.igb.menu.customgenome;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.FocusModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.miginfocom.layout.CC;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.tbee.javafx.scene.layout.MigPane;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true)
public class EditCustomGenomes implements MenuBarEntryProvider {

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
    private ObservableSet<GenomeVersion> genomeVersions;
    private ObservableList<GenomeVersion> genomeVersionList;

    public EditCustomGenomes() {
        menuItem = new WeightedMenuItem(2, "Edit genomes");
    }

    @Override
    public Optional<List<WeightedMenuItem>> getMenuItems() {
        final List<WeightedMenuItem> menuItems = Lists.newArrayList(menuItem);
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

        genomeVersions = genomeVersionRegistry.getRegisteredGenomeVersions();
        genomeVersionList = FXCollections.observableArrayList(genomeVersions);
        //Convert to lambda ?? 
        genomeVersions.addListener(new SetChangeListener<GenomeVersion>() {
            @Override
            public void onChanged(SetChangeListener.Change<? extends GenomeVersion> change) {
                if (change.wasAdded()) {
                    genomeVersionList.add(change.getElementAdded());
                } else if (change.wasRemoved()) {
                    genomeVersionList.remove(change.getElementRemoved());
                }
            }
        });

        for (Iterator<GenomeVersion> iterator = genomeVersions.iterator(); iterator.hasNext();) {
            GenomeVersion next = iterator.next();
        }

        migPane = new MigPane("fillx", "[]rel[grow]", "[][][]");
        stage = new Stage();
        stage.setTitle("Edit or delete custom genome");
        stage.setMinWidth(1800);
        stage.setMaxWidth(1800);
        stage.setMinHeight(500);
        stage.setMaxHeight(500);
        table = new TableView();

        table.setEditable(true);
        species = new TableColumn("Species");
        version = new TableColumn("Version");
        fileName = new TableColumn("File Name");
        editColumn = new TableColumn("Settings");
        deleteColumn = new TableColumn("Delete");
        table.getColumns().addAll(species, version, fileName, editColumn, deleteColumn);
        table.setItems(FXCollections.observableArrayList(genomeVersions));

        version.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
                return new SimpleStringProperty(param.getValue().getSpeciesName());
            }
        });
        species.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
                return new SimpleStringProperty(param.getValue().getName());
            }
        });
        fileName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<GenomeVersion, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<GenomeVersion, String> param) {
                String[] path = param.getValue().getReferenceSequenceProvider().getPath().split(File.separator);
                return new SimpleStringProperty(path[path.length - 1]);
            }
        }
        );
        cancelBtn = new Button("Exit");
        cancelBtn.setOnAction(event -> {
            stage.hide();
        });
        table.setOnMouseClicked(event -> {
            TablePosition focusedCell = table.getFocusModel().getFocusedCell();
            //delete genome
            if (focusedCell.getColumn() == 4) {
                customGenomePersistenceManager.deleteCustomGenome(genomeVersionList.get(focusedCell.getRow()));
            } //edit genome
            else if (focusedCell.getColumn() == 3) {
                GenomeVersion genome = genomeVersionList.get(focusedCell.getRow());

            }

        });
    }

    private void layoutComponents() {
        migPane.add(table, "wrap");
        migPane.add(cancelBtn, new CC().gap("rel").tag("ok").span(3).split());
        stage.setResizable(false);
        Scene scene = new Scene(migPane);
        stage.setScene(scene);
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
