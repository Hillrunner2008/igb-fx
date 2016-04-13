package org.lorainelab.igb.tabs.genome;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.visualization.tabs.api.TabDockingPosition;
import org.lorainelab.igb.visualization.tabs.api.TabProvider;

@Component(immediate = true)
public class GenomeAssemblyTab implements TabProvider {

    private static final String TAB_TITLE = "Current Genome";
    private final int TAB_WEIGHT = 0;
    private final Tab genomeAssemblyTab;
    @FXML
    private AnchorPane tabContent;
    @FXML
    private ComboBox speciesComboBox;
    @FXML
    private ComboBox<GenomeVersion> genomeVersionComboBox;
    @FXML
    private TableView<Chromosome> sequenceInfoTable;
    @FXML
    private TableColumn seqNameColumn;
    @FXML
    private TableColumn seqLengthColumn;
    private ObservableList<Chromosome> tableData;

    private GenomeVersionRegistry genomeVersionRegistry;

    public GenomeAssemblyTab() {
        tableData = FXCollections.observableArrayList();
        genomeAssemblyTab = new Tab(TAB_TITLE);
        final URL resource = GenomeAssemblyTab.class.getClassLoader().getResource("GenomeAssemblyTab.fxml");
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
        genomeAssemblyTab.setContent(tabContent);
        initializeSpeciesNameComboBox();
        initializeGenomeVersionComboBox();
        initializeSequenceTable();
    }

    private void initializeGenomeVersionComboBox() {
        genomeVersionComboBox.getItems().addAll(genomeVersionRegistry.getRegisteredGenomeVersions());
        genomeVersionRegistry.getRegisteredGenomeVersions().addListener((SetChangeListener.Change<? extends GenomeVersion> change) -> {
            if (change.wasAdded()) {
                genomeVersionComboBox.getItems().add(change.getElementAdded());
            } else {
                genomeVersionComboBox.getItems().remove(change.getElementAdded());
            }
        });
        genomeVersionComboBox.setConverter(new StringConverter<GenomeVersion>() {
            @Override
            public String toString(GenomeVersion genomeVersion) {
                return genomeVersion.getName();
            }

            @Override
            public GenomeVersion fromString(String genomeVersionString) {
                return genomeVersionComboBox.getItems().filtered(gv -> gv.getName().equals(genomeVersionString)).get(0);
            }
        });
        genomeVersionComboBox.setDisable(true);
        genomeVersionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            tableData.clear();
            tableData.addAll(newValue.getReferenceSequenceProvider().getChromosomes());
            newValue.getReferenceSequenceProvider().getChromosomes().stream()
                    .findFirst()
                    .ifPresent(chromosome -> {
                        sequenceInfoTable.getSelectionModel().select(chromosome);
                    });
        });
    }

    private void initializeSpeciesNameComboBox() {
        speciesComboBox.getItems().addAll(
                genomeVersionRegistry.getRegisteredGenomeVersions()
                .stream()
                .map(gv -> gv.getSpeciesName())
                .collect(Collectors.toList())
        );
        genomeVersionRegistry.getRegisteredGenomeVersions().addListener((SetChangeListener.Change<? extends GenomeVersion> change) -> {
            Platform.runLater(() -> {
                if (change.wasAdded()) {
                    speciesComboBox.getItems().add(change.getElementAdded().getSpeciesName());
                } else {
                    speciesComboBox.getItems().remove(change.getElementAdded().getSpeciesName());
                }
            });
        });
        speciesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                genomeVersionComboBox.setDisable(newValue.equals(speciesComboBox.getPromptText()));
            });
        });
    }

    @Override
    public Tab getTab() {
        return genomeAssemblyTab;
    }

    @Override
    public TabDockingPosition getTabDockingPosition() {
        return TabDockingPosition.RIGHT;
    }

    @Override
    public int getTabWeight() {
        return TAB_WEIGHT;
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }

    private void initializeSequenceTable() {
        sequenceInfoTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sequenceInfoTable.getSelectionModel().setCellSelectionEnabled(false);
        seqNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        seqLengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        sequenceInfoTable.setItems(tableData);
    }

}
