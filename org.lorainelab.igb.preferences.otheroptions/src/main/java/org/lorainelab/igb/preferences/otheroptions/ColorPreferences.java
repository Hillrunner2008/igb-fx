package org.lorainelab.igb.preferences.otheroptions;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.io.IOException;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.lorainelab.igb.data.model.util.Palette;
import org.lorainelab.igb.preferencemanager.api.PreferencesTabProvider;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.lorainelab.igb.view.api.ViewService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class ColorPreferences implements PreferencesTabProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ColorPreferences.class);
    private Tab tab;
    private BundleContext bc;

    @FXML
    private ColorPicker canvasBg;
    @FXML
    private ColorPicker loadedRegionHighlight;
    @FXML
    private ColorPicker glyphFg;
    @FXML
    private ColorPicker glyphLine;
    @FXML
    private ColorPicker annotationLabel;
    @FXML
    private ColorPicker a_color;
    @FXML
    private ColorPicker g_color;
    @FXML
    private ColorPicker t_color;
    @FXML
    private ColorPicker c_color;
    private ViewService viewService;
    private Runnable refreshAction;

    public ColorPreferences() {
        tab = new Tab("Color Options");
    }

    @Activate
    public void activate(BundleContext bc) {
        this.bc = bc;
        this.refreshAction = viewService.getRefreshViewAction();
        final URL resource = ColorPreferences.class.getClassLoader().getResource("ColorPreferences.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                StackPane root = fxmlLoader.load();
                root.getStylesheets().add(bc.getBundle().getEntry("colorPreferences.css").toExternalForm());
                tab.setContent(root);
                canvasBg.setValue(Palette.DEFAULT_CANVAS_BG);
                canvasBg.setOnAction((ActionEvent event) -> {
                    Color selectedColor = canvasBg.getValue();
                    Palette.DEFAULT_CANVAS_BG = selectedColor;
                    refreshAction.run();
                    //TODO persist to preferences
                });

                loadedRegionHighlight.setValue(Palette.LOADED_REGION_BG);
                loadedRegionHighlight.setOnAction((ActionEvent event) -> {
                    Color selectedColor = loadedRegionHighlight.getValue();
                    Palette.LOADED_REGION_BG = selectedColor;
                    refreshAction.run();
                    //TODO persist to preferences
                });

                glyphFg.setValue(Palette.DEFAULT_GLYPH_FILL);
                glyphFg.setOnAction((ActionEvent event) -> {
                    Color selectedColor = glyphFg.getValue();
                    Palette.DEFAULT_GLYPH_FILL = selectedColor;
                    refreshAction.run();
                    //TODO persist to preferences
                });
                
                glyphLine.setValue(Palette.DEFAULT_LINE_FILL);
                glyphLine.setOnAction((ActionEvent event) -> {
                    Color selectedColor = glyphLine.getValue();
                    Palette.DEFAULT_LINE_FILL = selectedColor;
                    refreshAction.run();
                    //TODO persist to preferences
                });

                annotationLabel.setValue(Palette.DEFAULT_LABEL_COLOR);
                annotationLabel.setOnAction((ActionEvent event) -> {
                    Color selectedColor = annotationLabel.getValue();
                    Palette.DEFAULT_LABEL_COLOR = selectedColor;
                    refreshAction.run();
                    //TODO persist to preferences
                });

                a_color.setValue(Palette.A_COLOR);
                a_color.setOnAction((ActionEvent event) -> {
                    Color selectedColor = a_color.getValue();
                    Palette.A_COLOR = selectedColor;
                    refreshAction.run();
                    //TODO persist to preferences
                });
                t_color.setValue(Palette.T_COLOR);
                t_color.setOnAction((ActionEvent event) -> {
                    Color selectedColor = t_color.getValue();
                    Palette.T_COLOR = selectedColor;
                    refreshAction.run();
                    //TODO persist to preferences
                });
                g_color.setValue(Palette.G_COLOR);
                g_color.setOnAction((ActionEvent event) -> {
                    Color selectedColor = g_color.getValue();
                    Palette.G_COLOR = selectedColor;
                    refreshAction.run();
                    //TODO persist to preferences
                });
                c_color.setValue(Palette.C_COLOR);
                c_color.setOnAction((ActionEvent event) -> {
                    Color selectedColor = c_color.getValue();
                    Palette.C_COLOR = selectedColor;
                    refreshAction.run();
                    //TODO persist to preferences
                });

            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        });

    }

    @Reference
    public void setViewService(ViewService viewService) {
        this.viewService = viewService;
    }

    @Override
    public Tab getPreferencesTab() {
        return tab;
    }

    @Override
    public int getTabWeight() {
        return 5;
    }

}
