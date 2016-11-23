package org.lorainelab.igb.quickload;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javafx.application.Platform;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.sf.image4j.codec.ico.ICODecoder;
import org.lorainelab.igb.cache.api.RemoteFileCacheService;
import org.lorainelab.igb.data.model.util.DataSourceUtilsImpl;
import org.lorainelab.igb.dataprovider.api.DataProvider;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.tabs.api.TabDockingPosition;
import org.lorainelab.igb.tabs.api.TabProvider;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class QuickloadFileBrowserTab extends Tab implements TabProvider {

    private static final Logger LOG = LoggerFactory.getLogger(QuickloadFileBrowserTab.class);
    private static final String TAB_TITLE = "Quickload Data";
    private static final int TAB_WEIGHT = 5;

    @FXML
    private VBox root;
    private TreeView<String> fileTree;

    private SetChangeListener<DataProvider> dataProviderChangeListener;
    private QuickloadSiteManager quickloadSiteManager;
    private SelectionInfoService selectionInfoService;
    private RemoteFileCacheService cacheService;

    public QuickloadFileBrowserTab() {
        setText(TAB_TITLE);
        final URL resource = QuickloadFileBrowserTab.class.getClassLoader().getResource("quickloadFileBrowser.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                fxmlLoader.load();
                treeRoot = new TreeItem<String>("Available Data");
                treeRoot.setExpanded(true);
                fileTree = new TreeView<>(treeRoot);
                VBox.setVgrow(fileTree, Priority.ALWAYS);
                root.getChildren().add(fileTree);
                setContent(root);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }
    private TreeItem<String> treeRoot;

    @Activate
    public void activate() {
        dataProviderChangeListener = (SetChangeListener.Change<? extends DataProvider> change) -> {
            Platform.runLater(() -> {
                if (change.wasAdded()) {
                    try {
                        DataProvider dataProvider = change.getElementAdded();
                        TreeItem<String> dataProviderRoot = new TreeItem<String>();
                        dataProviderRoot.valueProperty().bind(dataProvider.name());
                        dataProviderRoot.setExpanded(true);
                        String iconPath = dataProvider.url().get() + "favicon.ico";
                        if (DataSourceUtilsImpl.resourceAvailable(iconPath)) {
                            final URL iconUrl = new URL(iconPath);
                            cacheService.getFilebyUrl(iconUrl).ifPresent(icon -> {
                                try {
                                    List<BufferedImage> read = ICODecoder.read(icon);
                                    if (!read.isEmpty()) {
                                        final ImageView imageView = new ImageView(SwingFXUtils.toFXImage(read.get(0), null));
                                        imageView.setFitHeight(16);
                                        imageView.setFitWidth(16);
                                        dataProviderRoot.setGraphic(imageView);
                                    }
                                } catch (IOException ex) {
                                }
                            });
                        }
                        treeRoot.getChildren().add(dataProviderRoot);
                    } catch (MalformedURLException ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                } else {
                    DataProvider dataProvider = change.getElementRemoved();
                    List<TreeItem<String>> toRemove = treeRoot.getChildren().stream().filter(child -> child.getValue().equalsIgnoreCase(dataProvider.name().get())).collect(toList());
                    treeRoot.getChildren().removeAll(toRemove);
                }
            });
        };
        quickloadSiteManager.getDataProviders().addListener(new WeakSetChangeListener<>(dataProviderChangeListener));
    }

    @Override
    public Tab getTab() {
        return this;
    }

    @Override
    public TabDockingPosition getTabDockingPosition() {
        return TabDockingPosition.RIGHT;
    }

    @Override
    public int getTabWeight() {
        return TAB_WEIGHT;
    }

    @Reference
    public void setQuickloadSiteManager(QuickloadSiteManager quickloadSiteManager) {
        this.quickloadSiteManager = quickloadSiteManager;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference
    public void setCacheService(RemoteFileCacheService cacheService) {
        this.cacheService = cacheService;
    }

}
