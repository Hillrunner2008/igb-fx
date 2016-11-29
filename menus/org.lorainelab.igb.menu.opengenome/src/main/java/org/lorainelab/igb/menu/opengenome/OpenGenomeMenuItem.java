/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.opengenome;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta
 */
@Component(immediate = true)
public class OpenGenomeMenuItem implements MenuBarEntryProvider {

    private WeightedMenuItem menuItem;
    private Stage stage;
    private GenomeVersionRegistry genomeVersionRegistry;
    private static final Logger LOG = LoggerFactory.getLogger(OpenGenomeMenuItem.class);
    private ChangeListener<Optional<GenomeVersion>> selectedGenomeVersionChangeListener;
    private WeakChangeListener<Optional<GenomeVersion>> weakChangeListener;
    private final ObservableList<GenomeVersion> genomeVersionData;
    private ObservableList<String> speciesComboboxItems;

    @FXML
    AnchorPane anchorPanne;
    @FXML
    ComboBox<GenomeVersion> genomeVersionComboBox;
    @FXML
    ComboBox<String> speciesComboBox;
    @FXML
    Button openGenomeButton;
    @FXML
    Button cancelButton;

    public OpenGenomeMenuItem() {
        final URL resource = OpenGenomeMenuItem.class.getClassLoader().getResource("OpenGenome.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        genomeVersionData = FXCollections.observableArrayList();//(GenomeVersion gv) -> new Observable[]{gv.name()});
        speciesComboboxItems = FXCollections.observableArrayList();
        runAndWait(() -> {
            try {
                fxmlLoader.load();
                initComponents();

            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Activate
    public void activate() {
        menuItem = new WeightedMenuItem(1, "Open Genome\u2026");
        menuItem.setDisable(false);
        Platform.runLater(() -> {
            initializeSpeciesNameComboBox();
            initializeGenomeVersionComboBox();
        });
        menuItem.setOnAction(action -> {
            Platform.runLater(() -> {
                stage.show();
            });
        });
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

    private void initializeGenomeVersionComboBox() {
        genomeVersionData.addAll(genomeVersionRegistry.getRegisteredGenomeVersions());
        genomeVersionComboBox.setItems(genomeVersionData);
        genomeVersionComboBox.setConverter(new StringConverter<GenomeVersion>() {
            @Override
            public String toString(GenomeVersion genomeVersion) {
                return genomeVersion.name().get();
            }

            @Override
            public GenomeVersion fromString(String genomeVersionString) {
                return genomeVersionComboBox.getItems().filtered(gv -> gv.name().equals(genomeVersionString)).get(0);
            }
        });
        genomeVersionComboBox.setDisable(true);
    }

    private void initializeSpeciesNameComboBox() {
        speciesComboboxItems.clear();
        speciesComboboxItems.addAll(
                genomeVersionRegistry.getRegisteredGenomeVersions()
                        .stream()
                        .map(gv -> gv.getSpeciesName().get())
                        .collect(Collectors.toSet())
        );
        speciesComboBox.setItems(new SortedList<String>(speciesComboboxItems, Collator.getInstance()));

        genomeVersionRegistry.getRegisteredGenomeVersions().addListener((SetChangeListener.Change<? extends GenomeVersion> change) -> {
            Platform.runLater(() -> {
                if (change.wasAdded()) {
                    if (!speciesComboboxItems.contains(change.getElementAdded().getSpeciesName())) {
                        final String speciesName = change.getElementAdded().getSpeciesName().get();
                        if (!speciesComboboxItems.contains(speciesName)) {
                            speciesComboboxItems.add(speciesName);
                        }
                    } else {
                        //upde only version combo box
                        genomeVersionComboBox.getItems().add(change.getElementAdded());
                    }
                } else {
                    final GenomeVersion elementRemoved = change.getElementRemoved();
                    genomeVersionComboBox.getItems().add(elementRemoved);
                    long otherGenomeOfSameSpecies = genomeVersionRegistry.getRegisteredGenomeVersions().stream()
                            .filter(genomeVersion -> genomeVersion.getSpeciesName().get().equalsIgnoreCase(change.getElementRemoved().getSpeciesName().get()))
                            .count();
                    if (otherGenomeOfSameSpecies <= 0) {
                        final StringProperty speciesName = change.getElementRemoved().getSpeciesName();
                        if (speciesComboboxItems.contains(speciesName.get())) {
                            speciesComboboxItems.remove(speciesName.get());
                        }
                    }

                }
            });
        });

        speciesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            boolean disableGenomeVersionSelection = newValue == null || newValue.equals(speciesComboBox.getPromptText());
            Platform.runLater(() -> {
                if (!disableGenomeVersionSelection) {
                    genomeVersionComboBox.getItems().clear();
                    genomeVersionRegistry.getRegisteredGenomeVersions().stream()
                            .filter(genomeVersion -> genomeVersion.getSpeciesName().get().equalsIgnoreCase(newValue))
                            .forEach(genomeVersion -> genomeVersionComboBox.getItems().add(genomeVersion));
                } else {
                    genomeVersionRegistry.setSelectedGenomeVersion(null);
                }
                genomeVersionComboBox.setDisable(disableGenomeVersionSelection);
            });
        });
    }

    private void initComponents() {

        openGenomeButton.setOnAction(ae -> {
            GenomeVersion gv = genomeVersionComboBox.getSelectionModel().getSelectedItem();
            if (gv != null) {
                Platform.runLater(() -> {
                    genomeVersionRegistry.setSelectedGenomeVersion(gv);
                    stage.hide();
                });
            }
        });

        cancelButton.setOnAction(ae -> {
            Platform.runLater(() -> {
                stage.hide();
            });
        });

        Scene scene = new Scene(anchorPanne);
        stage = new Stage();
        stage.setResizable(false);
        stage.setScene(scene);
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }
}
