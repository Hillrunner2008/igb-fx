package org.lorainelab.igb.tabs.console;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javafx.scene.control.TextArea;

/**
 *
 * @author dcnorris
 */
public class ConsoleOutputStream extends OutputStream {

    TextArea ta;
    PrintStream original;

    /**
     * Creates an OutputStream that writes to the given TextArea.
     *
     * @param echo Can be null, or a PrintStream to which a copy of all output
     * will also by written. Thus you can send System.out to a text area and
     * also still send an echo to the original System.out.
     */
    public ConsoleOutputStream(TextArea t, PrintStream echo) {
        this.ta = t;
        this.original = echo;
    }

    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    @Override
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        ta.selectEnd();
        ta.appendText(new String(b, off, len, "UTF-8"));
        if (original != null) {
            original.write(b, off, len);
        }
    }
}
