package org.lorainelab.igb.visualization.util;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;

/**
 *
 * @author dcnorris
 */
public class FXUtilities {

    public static void runAndWait(Runnable runnable) {
        checkNotNull(runnable);
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            final CountDownLatch doneLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    runnable.run();
                } finally {
                    doneLatch.countDown();
                }
            });
            try {
                doneLatch.await();
            } catch (InterruptedException ex) {
            }
        }
    }

}
