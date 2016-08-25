package org.lorainelab.igb.tabs.console;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.apache.karaf.log.core.LogEventFormatter;
import org.apache.karaf.log.core.LogService;
import org.lorainelab.igb.tabs.api.TabDockingPosition;
import org.lorainelab.igb.tabs.api.TabProvider;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.framework.BundleContext;
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
    private static final String FATAL = "fatal";
    private static final String ERROR = "error";
    private static final String WARN = "warn";
    private static final String INFO = "info";
    private static final String DEBUG = "debug";
    private static final String TRACE = "trace";

    public ConsoleTab() {
        consoleTextArea = new TextArea();
        setupLoggingRedirects();
        StackPane pane = new StackPane(consoleTextArea);
        this.setContent(pane);
    }

    private void setupLoggingRedirects() {
        try {
            ConsoleOutputStream cs = new ConsoleOutputStream(consoleTextArea, System.out);
            System.setOut(new PrintStream(cs, false, "UTF-8"));
            System.setErr(new PrintStream(new ConsoleOutputStream(consoleTextArea, System.err), false, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Activate
    public void activate(BundleContext bc) {
        consoleTextArea.getStylesheets().add(bc.getBundle().getEntry("consoleStyle.css").toExternalForm());
        logService.addAppender(this);
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
//        String lvl = event.getLevel().toString().toLowerCase();
        final String logLine = eventFormatter.format(event, null, true);
        Platform.runLater(() -> {
            consoleTextArea.selectEnd();
            consoleTextArea.appendText(logLine);
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

    //TODO consider RichTextFX to allow coloring by level 
    private String getLvlColor(String lvl) {
        String color = "#BF4040";
        if (FATAL.equals(lvl)) {
            color = colorToHex(Color.RED);
        } else if (ERROR.equals(lvl)) {
            color = colorToHex(Color.RED);
        } else if (WARN.equals(lvl)) {
            color = colorToHex(Color.YELLOW);
        } else if (INFO.equals(lvl)) {
            color = "";
        } else if (DEBUG.equals(lvl)) {
            color = "#BF4040";
        } else if (TRACE.equals(lvl)) {
            color = colorToHex(Color.BLACK);
        }
        return color;
    }

    private String colorToHex(Color color) {
        String hex2 = "#" + Integer.toHexString(color.hashCode());
        return hex2;
    }

}
