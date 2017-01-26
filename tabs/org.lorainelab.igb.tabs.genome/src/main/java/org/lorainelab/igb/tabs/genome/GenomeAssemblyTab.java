package org.lorainelab.igb.tabs.genome;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.tabs.api.TabDockingPosition;
import org.lorainelab.igb.tabs.api.TabProvider;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class GenomeAssemblyTab implements TabProvider {

    private static final String TAB_TITLE = "Current Genome";
    private final int TAB_WEIGHT = 0;
    private final Tab genomeAssemblyTab;
    @FXML
    private AnchorPane tabContent;
//    @FXML
//    private ComboBox<String> speciesComboBox;
//    @FXML
//    private ComboBox<GenomeVersion> genomeVersionComboBox;
    @FXML
    private TableView<Chromosome> sequenceInfoTable;
    @FXML
    private TableColumn seqNameColumn;
    @FXML
    private TableColumn seqLengthColumn;
    private final ObservableList<Chromosome> tableData;
    private final ObservableList<GenomeVersion> genomeVersionData;

    private GenomeVersionRegistry genomeVersionRegistry;
    private static final Logger LOG = LoggerFactory.getLogger(GenomeAssemblyTab.class);

    public GenomeAssemblyTab() {
        speciesComboboxItems = FXCollections.observableArrayList();
        tableData = FXCollections.observableArrayList();
        genomeVersionData = FXCollections.observableArrayList((GenomeVersion gv) -> new Observable[]{gv.name()});
        genomeAssemblyTab = new Tab(TAB_TITLE);
    }

    @Activate
    public void activate() {
        final URL resource = GenomeAssemblyTab.class.getClassLoader().getResource("GenomeAssemblyTab.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                fxmlLoader.load();
                genomeAssemblyTab.setContent(tabContent);
                initializeSequenceTable();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private ChangeListener<GenomeVersion> genomeVersionComboboxChangeListener;
    private ChangeListener<Optional<GenomeVersion>> selectedGenomeVersionChangeListener;

    private void loadSelectedGenomeVersion(GenomeVersion selectedGenomeVersion) {
        Platform.runLater(() -> {
            synchronized (tableData) {
                tableData.clear();
                tableData.addAll(selectedGenomeVersion.getChromosomes());
            }
        });
        selectedGenomeVersion.getChromosomes().addListener((SetChangeListener.Change<? extends Chromosome> change) -> {
            Platform.runLater(() -> {
                synchronized (tableData) {
                    if (change.wasAdded()) {
                        tableData.add(change.getElementAdded());
                    } else {
                        tableData.remove(change.getElementRemoved());
                    }
                }
            });
        });
        sequenceInfoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedChromosome) -> {
            selectedGenomeVersion.setSelectedChromosome(selectedChromosome);
        });
        selectedGenomeVersion.getChromosomes().stream()
                .findFirst()
                .ifPresent(chromosome -> {
                    Platform.runLater(() -> {
                        sequenceInfoTable.getSelectionModel().select(chromosome);
                    });
                });
    }

    private ChangeListener<String> speciesComboBoxChangeListener;
    private ObservableList<String> speciesComboboxItems;

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
        seqNameColumn.prefWidthProperty().bind(sequenceInfoTable.widthProperty().multiply(.40));
        seqLengthColumn.prefWidthProperty().bind(sequenceInfoTable.widthProperty().multiply(.60));
        sequenceInfoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        selectedGenomeVersionChangeListener = (observable, oldValue, selectedGenomeVersion) -> {
            if (selectedGenomeVersion.isPresent()) {
                loadSelectedGenomeVersion(selectedGenomeVersion.get());
            } else {
                tableData.clear();
            }
        };
        genomeVersionRegistry.getSelectedGenomeVersion().addListener(new WeakChangeListener<>(selectedGenomeVersionChangeListener));
    }

}
