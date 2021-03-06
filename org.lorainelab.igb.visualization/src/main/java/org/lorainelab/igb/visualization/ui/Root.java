package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.lorainelab.igb.visualization.footer.Footer;
import org.lorainelab.igb.visualization.menubar.MenuBarManager;
import org.lorainelab.igb.visualization.toolbar.ToolBarManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = Root.class)
public class Root extends VBox {

    private static final Logger LOG = LoggerFactory.getLogger(Root.class);

    private StackPane stackPane;
    private MenuBarManager menuBarManager;
    private ToolBarManager toolbarProvider;
    private Footer footer;
    private MainSplitPane mainSplitPane;

    public Root() {
        stackPane = new StackPane();
        VBox.setVgrow(stackPane, Priority.ALWAYS);
    }

    @Activate
    public void activate() {
        Platform.runLater(() -> {
            stackPane.getChildren().add(mainSplitPane);
            getChildren().add(menuBarManager.getMenuBar());
            getChildren().add(toolbarProvider.getTopToolbar());
            getChildren().add(stackPane);
            getChildren().add(footer);
        });
    }

    @Reference(unbind = "removeMainSplitPane")
    public void setMainSplitPane(MainSplitPane mainSplitPane) {
        this.mainSplitPane = mainSplitPane;
    }

    public void removeMainSplitPane(MainSplitPane mainSplitPane) {
        LOG.info("removeMainSplitPane called");
        Platform.runLater(() -> {
            stackPane.getChildren().clear();
        });
    }

    @Reference(unbind = "removeMenuBarManager")
    public void setMenuBarManager(MenuBarManager menuBarManager) {
        this.menuBarManager = menuBarManager;
    }

    public void removeMenuBarManager(MenuBarManager menuBarManager) {
        LOG.info("removeMenuBarManager called");
        try {
            Platform.runLater(() -> {
                getChildren().remove(menuBarManager.getMenuBar());
            });
        } catch (Exception ex) {
            //do nothing
        }
    }

    @Reference(unbind = "removeToolbarManager")
    public void setToolbarProvider(ToolBarManager toolbarProvider) {
        this.toolbarProvider = toolbarProvider;
    }

    public void removeToolbarManager(ToolBarManager toolbarProvider) {
        Platform.runLater(() -> {
            getChildren().remove(toolbarProvider.getTopToolbar());
        });
    }

    @Reference(unbind = "removeFooter")
    public void setFooter(Footer footer) {
        this.footer = footer;
    }

    public void removeFooter(Footer footer) {
        try {
            Platform.runLater(() -> {
                getChildren().remove(footer);
            });
        } catch (Exception ex) {
            //do nothing
        }
    }

    @Deactivate
    private void deactivate() {
        try {
            Platform.runLater(() -> {
                getChildren().clear();
            });
        } catch (Exception ex) {
            //do nothing
        }
    }
}
