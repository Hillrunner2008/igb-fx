package org.lorainelab.igb.recentfiles.registry.api;

import javafx.beans.property.ReadOnlyListWrapper;

/**
 *
 * @author dcnorris
 */
public interface RecentFilesRegistry {

    ReadOnlyListWrapper<String> getRecentFiles();
    void addRecentFile(String recentFile);
    void clearRecentFiles();
}
