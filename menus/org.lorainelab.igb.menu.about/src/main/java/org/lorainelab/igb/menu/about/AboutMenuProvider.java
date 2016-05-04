/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.about;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.control.HyperlinkLabel;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.stage.provider.api.StageProvider;
import org.osgi.framework.BundleContext;

/**
 * FXML Controller class
 *
 * @author dfreese
 */
@Component(immediate = true)
public final class AboutMenuProvider implements MenuBarEntryProvider {

    private static final Links BIOVIZ_LINK = Links.BIOVIZ_LINK;
    private static final Links AFFYMETRIX_LINK = Links.AFFYMETRIX_LINK;
    private static final Links CITATION_LINK = Links.CITATION_LINK;

    private StackPane dialogPane;
    private Stage stage;
    private final HyperlinkLabel descriptionText;
    private final HyperlinkLabel citationText;
    private WeightedMenuItem menuItem;
    private HostServices hostServices;

    @FXML
    private Label titleLabel;

    @FXML
    private ImageView imageView;
    @FXML
    private StackPane mainTextContainer;
    @FXML
    private Button okBtn;
    private BundleContext bundleContext;
    private StageProvider stageProvider;

    public AboutMenuProvider() {
        menuItem = new WeightedMenuItem(1, "About");
        descriptionText = new HyperlinkLabel();
        citationText = new HyperlinkLabel();
    }

    private void createScene() {
        Platform.runLater(() -> {
            stage = new Stage();
            stage.setResizable(false);
            stage.setAlwaysOnTop(true);
            Image image = new Image(bundleContext.getBundle().getEntry("igb_128.png").toExternalForm());
            imageView.setImage(image);
            imageView.setFitHeight(80);
            imageView.setFitWidth(80);
            setTitleLabel();
            setDescriptions();
            Scene dialogScene = new Scene(dialogPane);
            stage.setScene(dialogScene);
        });
    }

    //TODO -change to update the version
    private void setTitleLabel() {
        titleLabel.setText("About Integrated Genome Browser- " + "new");
    }

    private void setDescriptions() {
        descriptionText.setText("IGB (pronouned ig-bee) is a fast, flexible, desktop genome browser first developed at [" + AFFYMETRIX_LINK.getValue() + "] "
                + "for tiling arrays. IGB is now an open source software supported by grants and donations. "
                + "To find out more, visit [" + BIOVIZ_LINK.getValue() + "].\n\n"
                + "If you use IGB in your research, please cite: Freese NH, Norris DC, Loraine AE. [" + CITATION_LINK.getValue() + "] "
                + "Bioinformatics.Epub:2016, March 16;1-7. 10.1093/bioinformatics/btw069.");
        mainTextContainer.getChildren().add(descriptionText);
    }

    @Activate
    private void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        hostServices = stageProvider.getHostServices();
        Platform.runLater(() -> {
            try {
                final URL resource = AboutMenuProvider.class.getClassLoader().getResource("AboutMenu.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(resource);
                fxmlLoader.setClassLoader(this.getClass().getClassLoader());
                fxmlLoader.setController(this);
                dialogPane = (StackPane) fxmlLoader.load();
                init();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

        });
    }

    public void init() {
        createScene();
        menuItem.setOnAction(event -> {
            Platform.runLater(() -> {
                stage.centerOnScreen();
                stage.show();
            });
        });
        okBtn.setOnAction(event -> {
            Platform.runLater(() -> {
                stage.hide();
            });
        });
        descriptionText.setOnAction(event -> {
            Platform.runLater(() -> {
                Hyperlink link = (Hyperlink) event.getSource();
                String str = link.getText();
                for (Links l : Links.values()) {
                    if (l.getValue().equals(str)) {
                        hostServices.showDocument(l.url());
                    }
                }
            });
        });

    }

    @Override
    public Optional<List<WeightedMenuItem>> getMenuItems() {
        final List<WeightedMenuItem> menuItems = Lists.newArrayList(menuItem);
        return Optional.of(menuItems);
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.HELP;
    }

    @Reference
    public void setStageProvider(StageProvider stageProvider) {
        this.stageProvider = stageProvider;
    }

}
