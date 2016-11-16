package org.lorainelab.igb.quickload;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.lorainelab.igb.preferencemanager.api.PreferencesTabProvider;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class QuickloadRepositoryPreferencesTabProvider extends Tab implements PreferencesTabProvider {

    private static final Logger LOG = LoggerFactory.getLogger(QuickloadRepositoryPreferencesTabProvider.class);
    private static int WEIGHT = 5;

    @FXML
    private TableView repoTable;

    @FXML
    private TableColumn refreshColumn;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn urlColumn;
    @FXML
    private TableColumn enabledColumn;

    @FXML
    private Button upBtn;
    @FXML
    private Button downBtn;
    @FXML
    private Button addBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button enterPasswordBtn;
    @FXML
    private Button removeBtn;

    //to ensure class import in manifest header
    private FontAwesomeIconView dummyIcon;

    public QuickloadRepositoryPreferencesTabProvider() {
        setText("Quickload Repositories");
    }

    @Activate
    public void activate() {
        final URL resource = QuickloadRepositoryPreferencesTabProvider.class.getClassLoader().getResource("repositoryManager.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                VBox root = fxmlLoader.load();
                repoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//                nameColumn.prefWidthProperty().bind(root.widthProperty().multiply(.50));
//                urlColumn.prefWidthProperty().bind(root.widthProperty().multiply(.50));
                setContent(root);
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        });
    }

    @Override

    public Tab getPreferencesTab() {
        return this;
    }

    @Override
    public int getTabWeight() {
        return WEIGHT;
    }

}
