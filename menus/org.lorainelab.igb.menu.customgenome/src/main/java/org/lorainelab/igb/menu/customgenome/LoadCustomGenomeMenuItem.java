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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.miginfocom.layout.CC;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.data.model.sequence.ReferenceSequenceProvider;
import org.lorainelab.igb.data.model.util.TwoBitParser;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class LoadCustomGenomeMenuItem implements MenuBarEntryProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LoadCustomGenomeMenuItem.class);
    private WeightedMenuItem menuItem;
    private Stage stage;
    private GenomeVersionRegistry genomeVersionRegistry;
    private Button refSeqBrowseBtn;
    private Button okBtn;
    private Button cancelBtn;
    private Label refSeqLabel;
    private TextField refSeqTextField;
    private Label speciesLabel;
    private TextField speciesTextField;
    private Label versionLabel;
    private TextField versionTextField;
    private MigPane migPane;
    private static int CUSTOM_GENOME_COUNTER = 1;
    private SelectionInfoService selectionInfoService;

    public LoadCustomGenomeMenuItem() {
        menuItem = new WeightedMenuItem(1, "Load Custom Genome");
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
        migPane = new MigPane("fillx", "[]rel[grow]", "[][][]");
        stage = new Stage();
        stage.setMinWidth(575);
        stage.setMaxWidth(575);
        stage.setMinHeight(175);
        stage.setMaxHeight(175);
        stage.setTitle("Open Genome from File");
        speciesLabel = new Label("Species");
        speciesTextField = new TextField();
        versionLabel = new Label("Genome Version");
        versionTextField = new TextField();
        refSeqLabel = new Label("Reference Sequence");
        refSeqTextField = new TextField();
        refSeqTextField.setEditable(false);
        refSeqBrowseBtn = new Button("Choose File\u2026");
        refSeqBrowseBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Sequence File");
            File homeDirectory = new File(System.getProperty("user.home"));
            fileChooser.setInitialDirectory(homeDirectory);
            addFileExtensionFilters(fileChooser);
            Optional.ofNullable(fileChooser.showOpenDialog(null)).ifPresent(selectedFile -> {
                try {
                    refSeqTextField.setText(selectedFile.getCanonicalPath());
                    if (Strings.isNullOrEmpty(versionTextField.getText())) {
                        versionTextField.setText("Custom Genome " + CUSTOM_GENOME_COUNTER);
                    }
                    if (Strings.isNullOrEmpty(speciesTextField.getText())) {
                        speciesTextField.setText("Custom Species " + CUSTOM_GENOME_COUNTER);
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            });
        });
        okBtn = new Button("Ok");
        okBtn.setOnAction(event -> {
            boolean[] customGenomeAdded = {false};
            CompletableFuture.runAsync(() -> {
                try {
                    final String speciesName = speciesTextField.textProperty().get();
                    final String versionName = versionTextField.textProperty().get();
                    final String sequenceFileUrl = refSeqTextField.textProperty().get();
                    if (!Strings.isNullOrEmpty(speciesName)
                            || !Strings.isNullOrEmpty(versionName)
                            || !Strings.isNullOrEmpty(sequenceFileUrl)) {
                        ReferenceSequenceProvider twoBitProvider = (ReferenceSequenceProvider) new TwoBitParser(sequenceFileUrl);
                        GenomeVersion customGenome = new GenomeVersion(versionName, speciesName, twoBitProvider, versionName);
                        customGenomeAdded[0] = genomeVersionRegistry.getRegisteredGenomeVersions().add(customGenome);
                        genomeVersionRegistry.setSelectedGenomeVersion(customGenome);
                    }
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }).whenComplete((result, ex) -> {
                //TODO show error in popup instead of hiding if it occurred
                Platform.runLater(() -> {
                    if (customGenomeAdded[0]) {
                        Alert dlg = new Alert(AlertType.INFORMATION, "Custom Genome is Now available");
                        dlg.setWidth(600);
                        dlg.initModality(stage.getModality());
                        dlg.initOwner(stage.getOwner());
                        dlg.setTitle("Genome Added Successfully");
                        dlg.show();
                        dlg.setOnCloseRequest(closeEvent -> {
                            stage.hide();
                        });
                    } else {
                        stage.hide();
                    }
                });
            });
        });
        cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(event -> {
            stage.hide();
        });
    }

    private void layoutComponents() {
        migPane.add(refSeqLabel);
        migPane.add(refSeqTextField, "growx");
        migPane.add(refSeqBrowseBtn, "wrap");
        migPane.add(versionLabel, "");
        migPane.add(versionTextField, "growx, wrap");
        migPane.add(speciesLabel, "");
        migPane.add(speciesTextField, "growx, wrap");
        migPane.add(okBtn, new CC().gap("rel").tag("ok").span(3).split());
        migPane.add(cancelBtn, new CC().tag("ok"));
        stage.setResizable(false);
        Scene scene = new Scene(migPane);
        stage.setScene(scene);
    }

    @Override

    public Optional<List<WeightedMenuItem>> getMenuItems() {
        final List<WeightedMenuItem> menuItems = Lists.newArrayList(menuItem);
        return Optional.of(menuItems);
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.FILE;
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
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
