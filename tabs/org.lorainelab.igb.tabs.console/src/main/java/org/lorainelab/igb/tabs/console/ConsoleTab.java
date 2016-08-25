package org.lorainelab.igb.tabs.console;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import org.apache.karaf.log.core.LogEventFormatter;
import org.apache.karaf.log.core.LogService;
import org.lorainelab.igb.tabs.api.TabDockingPosition;
import org.lorainelab.igb.tabs.api.TabProvider;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, provide = {TabProvider.class})
public class ConsoleTab extends Tab implements TabProvider, PaxAppender {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleTab.class);
    private static final String TAB_TITLE = "Console";
    private final int TAB_WEIGHT = 1;
    private TextArea consoleTextArea;
    private LogService logService;
    private LogEventFormatter eventFormatter;

    public ConsoleTab() {
        consoleTextArea = new TextArea();
        StackPane pane = new StackPane(consoleTextArea);
        this.setContent(pane);
    }

    @Activate
    public void activate() {
        logService.addAppender(this);
        LOG.info("Logging GUI Console Initialized");
        System.out.println("testing system out");
        System.err.println("testing system err");
    }

    @Override
    public Tab getTab() {
        setText(TAB_TITLE);
        return this;
    }

    @Override
    public TabDockingPosition getTabDockingPosition() {
        return TabDockingPosition.BOTTOM;
    }

    @Override
    public int getTabWeight() {
        return TAB_WEIGHT;
    }

    @Override
    public void doAppend(PaxLoggingEvent event) {
        Platform.runLater(() -> {
            consoleTextArea.selectEnd();
            consoleTextArea.appendText(eventFormatter.format(event, null, true));
        });
    }

    @Reference
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @Reference
    public void setEventFormatter(LogEventFormatter eventFormatter) {
        this.eventFormatter = eventFormatter;
    }

}
