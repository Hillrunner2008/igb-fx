package org.lorainelab.igb.visualization.track;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.lorainelab.igb.data.model.StackedGlyphTrack;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.filehandler.api.DataType;
import org.lorainelab.igb.menu.api.TrackLabelContextMenuEntryProvider;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class StackHeightAdjustmentContextMenuEntry implements TrackLabelContextMenuEntryProvider {

    private static final Logger LOG = LoggerFactory.getLogger(StackHeightAdjustmentContextMenuEntry.class);
    @FXML
    private TextField stackHeightEntryField;
    @FXML
    private Button okBtn;
    @FXML
    private Button cancelBtn;
    private Stage stage;

    private BundleContext bc;

    public StackHeightAdjustmentContextMenuEntry() {
    }

    @Activate
    public void activate(BundleContext bc) {
        this.bc = bc;
        final URL resource = StackHeightAdjustmentContextMenuEntry.class.getClassLoader().getResource("stackHeightAdjustmentPane.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                BorderPane root = fxmlLoader.load();
                stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Set Track Stack Height");
                stage.setResizable(false);
                Scene scene = new Scene(root);
                stage.setScene(scene);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });

    }

    @Override
    public Set<DataType> getSupportedDataTypes() {
        return Sets.newHashSet(DataType.ANNOTATION, DataType.ALIGNMENT);
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems(Track track, Runnable refreshAction) {
        if (track instanceof StackedGlyphTrack) {
            StackedGlyphTrack stackedGlyphTrack = (StackedGlyphTrack) track;
            WeightedMenuItem adjustStackHeightMenuItem = new WeightedMenuItem(0);
            adjustStackHeightMenuItem.setText("Set Stack Height...");
            stackHeightEntryFieldChangeListener = (observable, oldValue, newValue) -> {
                stackHeightEntryField.setText(CharMatcher.inRange('0', '9').retainFrom(newValue));
            };

            stackHeightEntryField.textProperty().addListener(new WeakChangeListener<>(stackHeightEntryFieldChangeListener));
            stackHeightEntryField.setOnKeyPressed((KeyEvent ke) -> {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    adjustStackHeightAction(stackedGlyphTrack, refreshAction);
                }
            });
            okBtn.setOnAction(event -> {
                adjustStackHeightAction(stackedGlyphTrack, refreshAction);
            });
            cancelBtn.setOnAction(event -> {
                stage.hide();
            });

            adjustStackHeightMenuItem.setOnAction(action -> {
                stackHeightEntryField.setText("" + stackedGlyphTrack.getStackHeight());
                stage.show();
            });
            return Optional.ofNullable(Lists.newArrayList(adjustStackHeightMenuItem));
        }
        return Optional.empty();
    }
    private ChangeListener<String> stackHeightEntryFieldChangeListener;

    private void adjustStackHeightAction(StackedGlyphTrack stackedGlyphTrack, Runnable refreshAction) {
        stage.hide();
        CompletableFuture.supplyAsync(() -> {
            stackedGlyphTrack.setMaxStackHeight(Integer.parseInt(stackHeightEntryField.getText()));
            return null;
        }).whenComplete((u, t) -> {
            Platform.runLater(() -> {
                refreshAction.run();
            });
            if (t != null) {
                Throwable ex = (Throwable) t;
                LOG.error(ex.getMessage(), ex);
            }
        });
    }
}
