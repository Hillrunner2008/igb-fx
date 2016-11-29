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
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Comparator;
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
import javafx.collections.WeakSetChangeListener;
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
    private final ObservableList<GenomeVersion> genomeVersionData;
    private ObservableList<String> speciesComboboxItems;
    private SetChangeListener<GenomeVersion> vertionChangeListner;
    private ChangeListener<String> speciesChangeListner;

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
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
        menuItem = new WeightedMenuItem(1, "Open Genome\u2026");
    }

    @Activate
    public void activate() {
        Platform.runLater(() -> {
            initComponents();
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

        speciesComboBox.setItems(new SortedList<String>(speciesComboboxItems, Collator.getInstance()));

        speciesChangeListner = (observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                boolean disableGenomeVersionSelection = newValue == null || newValue.equals(speciesComboBox.getPromptText());
                if (!disableGenomeVersionSelection) {
                    genomeVersionComboBox.getItems().clear();
                    genomeVersionRegistry.getRegisteredGenomeVersions().stream()
                            .filter(genomeVersion -> genomeVersion.getSpeciesName().get().equalsIgnoreCase(newValue))
                            .sorted(new Comparator<GenomeVersion>() {
                                SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM_yyyy");

                                @Override
                                public int compare(GenomeVersion o1, GenomeVersion o2) {
                                    try {
                                        String name1 = o1.name().get();
                                        String name2 = o2.name().get();
                                        return -1 * dateFormatter.parse(name1, new ParsePosition(name1.length() - 8)).compareTo(dateFormatter.parse(name2, new ParsePosition(name2.length() - 8)));
                                    } catch (Exception e) {
                                        return 0;
                                    }
                                }
                            })
                            .forEach(genomeVersion -> genomeVersionComboBox.getItems().add(genomeVersion));
                } else {
                    genomeVersionRegistry.setSelectedGenomeVersion(null);
                }
                genomeVersionComboBox.setDisable(disableGenomeVersionSelection);
            });
        };

        speciesComboBox.valueProperty().addListener(new WeakChangeListener<>(speciesChangeListner));
    }

    private void initComponents() {

        vertionChangeListner = (SetChangeListener.Change<? extends GenomeVersion> change) -> {
            Platform.runLater(() -> {
                if (change.wasAdded() && !change.getElementAdded().isCustom()) {
                    if (!speciesComboboxItems.contains(change.getElementAdded().getSpeciesName())) {
                        final String speciesName = change.getElementAdded().getSpeciesName().get();
                        if (!speciesComboboxItems.contains(speciesName)) {
                            speciesComboboxItems.add(speciesName);
                        }
                    } else {
                        //upde only version combo box
                        genomeVersionComboBox.getItems().add(change.getElementAdded());
                    }
                } else if (change.wasRemoved() && !change.getElementRemoved().isCustom()) {
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
        };

        genomeVersionRegistry.getRegisteredGenomeVersions().addListener(new WeakSetChangeListener<>(vertionChangeListner));

        genomeVersionData.addAll(genomeVersionRegistry.getRegisteredGenomeVersions().stream().filter(gv -> !gv.isCustom()).collect(Collectors.toSet()));
        speciesComboboxItems.addAll(
                genomeVersionRegistry.getRegisteredGenomeVersions()
                        .stream()
                        .filter(gv -> !gv.isCustom())
                        .map(gv -> gv.getSpeciesName().get())
                        .collect(Collectors.toSet())
        );

        openGenomeButton.setOnAction(ae -> {
            Platform.runLater(() -> {
                GenomeVersion gv = genomeVersionComboBox.getSelectionModel().getSelectedItem();
                if (gv != null) {
                    genomeVersionRegistry.setSelectedGenomeVersion(gv);
                    stage.hide();
                }
            });
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
