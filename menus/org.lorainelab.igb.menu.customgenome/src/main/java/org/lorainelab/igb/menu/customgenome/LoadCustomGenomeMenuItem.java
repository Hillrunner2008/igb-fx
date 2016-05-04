package org.lorainelab.igb.menu.customgenome;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
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
    private Button refSeqBrowseButton;
    private Label refSeqLabel;
    private TextField refSeqTextField;
    private Label speciesLabel;
    private TextField speciesTextField;
    private Label versionLabel;
    private TextField versionTextField;
    private MigPane migPane;
    private static int CUSTOM_GENOME_COUNTER = 1;

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
        refSeqBrowseButton = new Button("Choose File\u2026");
        refSeqBrowseButton.setOnAction(event -> {
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
    }

    private void layoutComponents() {
        migPane.add(refSeqLabel);
        migPane.add(refSeqTextField, "growx");
        migPane.add(refSeqBrowseButton, "wrap");
        migPane.add(versionLabel, "");
        migPane.add(versionTextField, "growx, wrap");
        migPane.add(speciesLabel, "");
        migPane.add(speciesTextField, "growx");
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
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

    private void addFileExtensionFilters(FileChooser fileChooser) {
        //TODO setup dynamic registry for ReferenceSequenceProvider ... will require ReferenceSequenceProvider to hold supported extensions
        FileChooser.ExtensionFilter twoBitExtensionFilter = new FileChooser.ExtensionFilter("2bit", Lists.newArrayList("*.2bit"));
        fileChooser.getExtensionFilters().add(twoBitExtensionFilter);
    }
}
