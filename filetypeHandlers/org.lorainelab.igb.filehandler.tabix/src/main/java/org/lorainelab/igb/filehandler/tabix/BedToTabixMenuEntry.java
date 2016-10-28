package org.lorainelab.igb.filehandler.tabix;

import aQute.bnd.annotation.component.Component;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import static htsjdk.tribble.util.TabixUtils.STANDARD_INDEX_EXTENSION;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.preferences.SessionPreferences;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class BedToTabixMenuEntry implements MenuBarEntryProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BedToTabixMenuEntry.class);
    private WeightedMenuItem menuItem;

    @FXML
    private TextField inputFileTextField;
    @FXML
    private TextField outputFolderTextField;
    @FXML
    private Button selectInputFileBtn;
    @FXML
    private Button selectOutputFolderBtn;
    @FXML
    private Button convertBtn;
    @FXML
    private Button closeBtn;
    @FXML
    private BorderPane root;
    private Stage stage;

    public BedToTabixMenuEntry() {
        menuItem = new WeightedMenuItem(1, "Create tabix indexed bed file");
        menuItems = Lists.newArrayList(menuItem);
        runAndWait(() -> {
            try {
                final URL resource = BedToTabixMenuEntry.class.getClassLoader().getResource("bedToTabix.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(resource);
                fxmlLoader.setClassLoader(this.getClass().getClassLoader());
                fxmlLoader.setController(this);
                root = fxmlLoader.load();
                stage = new Stage();
                Scene scene = new Scene(root);
                stage.sizeToScene();
                stage.setScene(scene);
                initComponents();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private void initComponents() {
        selectInputFileBtn.setOnAction(evt -> {
            FileChooser fileChooser = getFileChooser();
            Optional.ofNullable(fileChooser.showOpenDialog(null)).ifPresent(selectedFile -> {
                inputFileTextField.setText(selectedFile.getAbsolutePath());
            });
        });
        selectOutputFolderBtn.setOnAction(evt -> {
            FileChooser chooser = getFileChooser();
            DirectoryChooser dc = new DirectoryChooser();
            dc.setInitialDirectory(chooser.getInitialDirectory());
            Optional.ofNullable(dc.showDialog(null)).ifPresent(saveDir -> {
                outputFolderTextField.setText(saveDir.getAbsolutePath());
            });
        });
        convertBtn.setOnAction(evt -> {
            String inputBedFile = inputFileTextField.getText().trim();
            String outputFolder = outputFolderTextField.getText().trim();
            if (Strings.isNullOrEmpty(inputBedFile) || !Strings.isNullOrEmpty(outputFolder)) {
                File sortedBedFile = new File(inputBedFile);
                final String outputFileName = outputFolder + File.separator + Files.getNameWithoutExtension(inputBedFile) + ".bed.gz";
                File outputBedFile = new File(outputFileName);
                if (sortedBedFile.exists()) {
                    try {
                        BedToTabixWriter.sortedBedToBGzip(sortedBedFile, outputBedFile);
                        File bgZippedInputFile = new File(outputFileName);
                        File outputIndexFile = new File(outputFileName + STANDARD_INDEX_EXTENSION);
                        BedToTabixWriter.writeIndex(bgZippedInputFile, sortedBedFile);
                    } catch (IOException ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                }
            }
        });
        closeBtn.setOnAction(evt -> {
            Platform.runLater(() -> {
                stage.hide();
            });
        });
        menuItem.setOnAction(evt -> {
            Platform.runLater(() -> {
                outputFolderTextField.setText("");
                inputFileTextField.setText("");
                stage.sizeToScene();
                stage.show();
            });
        });
    }

    private FileChooser getFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load File");
        File toOpen;
        if (SessionPreferences.getRecentSelectedFilePath() != null) {
            File file = new File(SessionPreferences.getRecentSelectedFilePath());
            String simpleFileName = file.getParent();
            toOpen = new File(simpleFileName);
        } else {
            toOpen = new File(System.getProperty("user.home"));
        }
        fileChooser.setInitialDirectory(toOpen);
        return fileChooser;
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems() {
        return Optional.of(menuItems);
    }
    private List<WeightedMenuEntry> menuItems;

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.TOOLS;
    }

}
