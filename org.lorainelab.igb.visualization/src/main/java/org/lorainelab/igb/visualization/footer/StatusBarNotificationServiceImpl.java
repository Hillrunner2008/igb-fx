package org.lorainelab.igb.visualization.footer;

import aQute.bnd.annotation.component.Component;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import org.controlsfx.control.StatusBar;
import org.lorainelab.igb.notifications.api.StatusBarNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = {StatusBarProvider.class, StatusBarNotificationService.class})
public class StatusBarNotificationServiceImpl implements StatusBarProvider, StatusBarNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(StatusBarNotificationServiceImpl.class);
    Set<CompletableFuture<?>> activeTasks;
    private StatusBar statusBar;

    public StatusBarNotificationServiceImpl() {
        statusBar = new StatusBar();
        statusBar.textProperty().setValue("");
        statusBar.progressProperty().setValue(0);
        activeTasks = Sets.newLinkedHashSet();
    }

    @Override
    public StatusBar getStatusBar() {
        return statusBar;
    }

// TODO accept integer property to track progress and text property to track text changes
    @Override
    public void submitTask(CompletableFuture<?> task) {
        activeTasks.add(task);
        statusBar.textProperty().setValue("Loading Human Genome");
        statusBar.progressProperty().setValue(20);
        task.whenComplete((result, ex) -> {
            activeTasks.remove(task);
            Platform.runLater(() -> {
                statusBar.textProperty().setValue("");
                statusBar.progressProperty().setValue(0);
            });
            if (ex != null) {
                LOG.error(ex.getMessage(), ex);
            }
        });
    }

}
