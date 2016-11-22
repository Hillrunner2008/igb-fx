package org.lorainelab.igb.quickload;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.lorainelab.igb.dataprovider.api.DataProvider;
import org.lorainelab.igb.preferences.SessionPreferences;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class AddEditeQuickoadModal {

    private static final Logger LOG = LoggerFactory.getLogger(AddEditeQuickoadModal.class);
    private Stage stage;

    @FXML
    private TextField nameField;
    @FXML
    private TextField urlField;
    @FXML
    private Button chooseFolderBtn;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;
    private final Optional<DataProvider> dataProvider;
    private final QuickloadDataProviderFactory dataProviderFactory;
    private int maxLoadPriority;

    public AddEditeQuickoadModal(Optional<DataProvider> dataProvider, QuickloadDataProviderFactory dataProviderFactory, int maxLoadPriority) {
        this.dataProvider = dataProvider;
        this.dataProviderFactory = dataProviderFactory;
        this.maxLoadPriority = maxLoadPriority;
        final URL resource = AddEditeQuickoadModal.class.getClassLoader().getResource("editDataSource.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                VBox root = fxmlLoader.load();
                stage = new Stage();
                stage.setResizable(false);
                stage.setAlwaysOnTop(true);
                Scene scene = new Scene(root);
                stage.setScene(scene);

                dataProvider.ifPresent(dp -> {
                    nameField.setText(dp.name().get());
                    urlField.setText(dp.url().get());
                });
                initializeBtnActions();
                stage.showAndWait();
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        });
    }

    private void initializeBtnActions() {
        chooseFolderBtn.setOnAction(action -> {
            FileChooser chooser = getFileChooser();
            DirectoryChooser dc = new DirectoryChooser();
            dc.setInitialDirectory(chooser.getInitialDirectory());
            Optional.ofNullable(dc.showDialog(null)).ifPresent(saveDir -> {
                Platform.runLater(() -> {
                    urlField.setText(saveDir.getAbsolutePath());
                });
            });
        });
        saveBtn.setOnAction(action -> {
            try {
                if (dataProvider.isPresent()) {
                    String path = urlField.getText();
                    String name = nameField.getText();
                    URL url = null;
                    if (path.startsWith("http") || path.startsWith("file:")) {
                        url = new URL(path);
                    } else {
                        url = new File(path).toURI().toURL();
                    }
                    dataProvider.get().name().set(name);
                    dataProvider.get().url().set(path);
                } else {
                    String path = urlField.getText();
                    String name = nameField.getText();
                    URL url = null;
                    if (path.startsWith("http") || path.startsWith("file:")) {
                        url = new URL(path);
                    } else {
                        url = new File(path).toURI().toURL();
                    }
                    dataProviderFactory.createDataProvider(path, name, maxLoadPriority);
                }
                Platform.runLater(() -> {
                    stage.hide();
                });
            } catch (Exception ex) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Invalid Url");
                alert.setContentText("Invalid Url format");
                alert.showAndWait();
            }
        });
        cancelBtn.setOnAction(action -> {
            Platform.runLater(() -> {
                stage.hide();
            });
        });
    }

    private FileChooser getFileChooser() {
        FileChooser fileChooser = new FileChooser();
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

}
