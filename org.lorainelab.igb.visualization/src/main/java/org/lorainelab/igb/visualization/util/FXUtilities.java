package org.lorainelab.igb.visualization.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;

/**
 *
 * @author dcnorris
 */
public class FXUtilities {

    public static void runAndWait(Runnable runnable) throws InterruptedException, ExecutionException {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            FutureTask<Void> future = new FutureTask<>(runnable, null);
            Platform.runLater(future);
            future.get();
        }
    }

}
