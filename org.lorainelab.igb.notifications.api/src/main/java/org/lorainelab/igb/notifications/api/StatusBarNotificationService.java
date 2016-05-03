package org.lorainelab.igb.notifications.api;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author dcnorris
 */
public interface StatusBarNotificationService {

     void submitTask(CompletableFuture<?> task);
}
