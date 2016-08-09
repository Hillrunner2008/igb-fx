package org.lorainelab.igb.recentfiles.registry.api;

import javafx.collections.ObservableSet;

/**
 *
 * @author dcnorris
 */
public interface RecentFilesRegistry {

    ObservableSet<String> getRecentFiles();
}
