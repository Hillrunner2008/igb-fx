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
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
import org.lorainelab.igb.recentgenome.registry.RecentGenomeRegistry;
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
    private String recentFilePath = null;
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
    private CustomGenomePersistenceManager customGenomePersistenceManager;
    private RecentGenomeRegistry recentGenomeRegistry;

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
                    clearState();
                    stage.centerOnScreen();
                    stage.show();
                });
            });

        });
    }

    private void initComponents() {
        migPane = new MigPane("fillx", "[]rel[grow]", "[][][]");
        stage = new Stage();
        stage.setMinWidth(440);
        stage.setMinHeight(165);
        stage.setHeight(165);
        stage.setMaxHeight(165);
        stage.setResizable(true);
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
            versionTextField.requestFocus();

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
                        final Optional<GenomeVersion> duplicate = isDuplicate(sequenceFileUrl);

                        if (!duplicate.isPresent()) {
                            ReferenceSequenceProvider twoBitProvider = (ReferenceSequenceProvider) new TwoBitParser(sequenceFileUrl);
                            GenomeVersion customGenome = new GenomeVersion(versionName, speciesName, twoBitProvider, versionName);
                            SessionPreferences.setRecentSelectedFilePath(sequenceFileUrl);
                            customGenomePersistenceManager.persistCustomGenome(customGenome);
                            SessionPreferences.setRecentSelectedFilePath(sequenceFileUrl);
                            customGenomeAdded[0] = genomeVersionRegistry.getRegisteredGenomeVersions().add(customGenome);
                            genomeVersionRegistry.setSelectedGenomeVersion(customGenome);
                            recentGenomeRegistry.addRecentGenome(customGenome);
                        } else {
                            Platform.runLater(() -> {
                                ButtonType switchBtn = new ButtonType("Switch");
                                ButtonType cancelBtn = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
                                Alert dlg = new Alert(AlertType.CONFIRMATION, "This sequence file is already mapped to the \n\""
                                        + duplicate.get().getName().get() + "\" genome."
                                        + "\n Choose Switch to load it.");
                                dlg.initModality(stage.getModality());
                                dlg.initOwner(stage.getOwner());
                                dlg.setTitle("Cannot add duplicate genome version");
                                dlg.getButtonTypes().setAll(switchBtn, cancelBtn);
                                Optional<ButtonType> result = dlg.showAndWait();
                                if (result.get() == switchBtn) {
                                    genomeVersionRegistry.setSelectedGenomeVersion(duplicate.get());
                                }
                            });
                        }
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

        refSeqTextField.setOnKeyPressed(this::handleKeyEvent);
        speciesTextField.setOnKeyPressed(this::handleKeyEvent);
        versionTextField.setOnKeyPressed(this::handleKeyEvent);
    }

    private void handleKeyEvent(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
                okBtn.fire();
                break;
            case ESCAPE:
                cancelBtn.fire();
                break;
            case K:
                if (event.isControlDown()) {
                    ((TextField) event.getSource()).deleteText(((TextField) event.getSource()).getCaretPosition(), ((TextField) event.getSource()).getLength());
                }
        }
    }

    private void layoutComponents() {
        migPane.add(refSeqLabel);
        migPane.add(refSeqTextField, "growx");
        migPane.add(refSeqBrowseBtn, "wrap");
        migPane.add(speciesLabel, "");
        migPane.add(speciesTextField, "growx, wrap");
        migPane.add(versionLabel, "");
        migPane.add(versionTextField, "growx, wrap");
        migPane.add(okBtn, new CC().gap("rel").x("container.x+150").span(3).tag("ok").split());
        migPane.add(cancelBtn, new CC().x("container.x+200").tag("ok"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(true);
        Scene scene = new Scene(migPane);
        stage.setScene(scene);
        stage.sizeToScene();
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
    public void setRecentGenomeRegistry(RecentGenomeRegistry recentGenomeRegistry) {
        this.recentGenomeRegistry = recentGenomeRegistry;
    }

    private void addFileExtensionFilters(FileChooser fileChooser) {
        //TODO setup dynamic registry for ReferenceSequenceProvider ... will require ReferenceSequenceProvider to hold supported extensions
        FileChooser.ExtensionFilter twoBitExtensionFilter = new FileChooser.ExtensionFilter("2bit", Lists.newArrayList("*.2bit"));
        fileChooser.getExtensionFilters().add(twoBitExtensionFilter);
    }

    private void clearState() {
        refSeqTextField.clear();
        versionTextField.clear();
        speciesTextField.clear();
    }

    private Optional<GenomeVersion> isDuplicate(String sequenceFileUrl) {
        return genomeVersionRegistry.getRegisteredGenomeVersions().stream()
                .filter(gv -> gv.getReferenceSequenceProvider().getPath().equalsIgnoreCase(sequenceFileUrl))
                .findFirst();
    }
}
