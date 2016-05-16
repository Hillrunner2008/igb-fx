package org.lorainelab.igb.tabs.genome;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import org.controlsfx.control.MaskerPane;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.tabs.api.TabDockingPosition;
import org.lorainelab.igb.tabs.api.TabProvider;

@Component(immediate = true)
public class GenomeAssemblyTab implements TabProvider {

    private static final String TAB_TITLE = "Current Genome";
    private final int TAB_WEIGHT = 0;
    private final Tab genomeAssemblyTab;
    @FXML
    private MaskerPane maskerPane;
    @FXML
    private AnchorPane tabContent;
    @FXML
    private ComboBox<String> speciesComboBox;
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
        maskerPane.setText("Waiting for Genome Versions...");
        genomeAssemblyTab.setContent(tabContent);
        initializeSpeciesNameComboBox();
        initializeGenomeVersionComboBox();
        initializeSequenceTable();
        initializeMaskerPane();
    }

    private void initializeMaskerPane() {
        maskerPane.setVisible(genomeVersionComboBox.getItems().isEmpty());
        genomeVersionComboBox.getItems().addListener((ListChangeListener.Change<? extends GenomeVersion> c) -> {
            maskerPane.setVisible(genomeVersionComboBox.getItems().isEmpty());
        });
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
        genomeVersionComboBox.valueProperty().addListener((observable, oldValue, selectedGenomeVersion) -> {
            genomeVersionRegistry.setSelectedGenomeVersion(selectedGenomeVersion);
        });
        selectedGenomeVersionChangeListener = (observable, oldValue, newValue) -> {
            newValue.ifPresent(selectedGenomeVersion -> {
                loadSelectedGenomeVersion(selectedGenomeVersion);
            });
        };
        genomeVersionRegistry.getSelectedGenomeVersion().addListener(selectedGenomeVersionChangeListener);
    }
    private ChangeListener<Optional<GenomeVersion>> selectedGenomeVersionChangeListener;

    private void loadSelectedGenomeVersion(GenomeVersion selectedGenomeVersion) {
        Platform.runLater(() -> {
            genomeVersionRegistry.getSelectedGenomeVersion().removeListener(selectedGenomeVersionChangeListener);
            genomeVersionComboBox.setValue(selectedGenomeVersion);
            genomeVersionRegistry.getSelectedGenomeVersion().addListener(selectedGenomeVersionChangeListener);
            speciesComboBox.setValue(selectedGenomeVersion.getSpeciesName());
            tableData.clear();
            tableData.addAll(selectedGenomeVersion.getReferenceSequenceProvider().getChromosomes());
        });
        selectedGenomeVersion.getReferenceSequenceProvider().getChromosomes().addListener((SetChangeListener.Change<? extends Chromosome> change) -> {
            Platform.runLater(() -> {
                if (change.wasAdded()) {
                    tableData.add(change.getElementAdded());
                } else {
                    tableData.remove(change.getElementRemoved());
                }
            });
        });
        sequenceInfoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedChromosome) -> {
            selectedGenomeVersion.setSelectedChromosome(selectedChromosome);
        });
        selectedGenomeVersion.getReferenceSequenceProvider().getChromosomes().stream()
                .findFirst()
                .ifPresent(chromosome -> {
                    Platform.runLater(() -> {
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
        sequenceInfoTable.setPlaceholder(new Label(""));
        sequenceInfoTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sequenceInfoTable.getSelectionModel().setCellSelectionEnabled(false);
        seqNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        seqLengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
        sequenceInfoTable.setItems(tableData);
    }

}
