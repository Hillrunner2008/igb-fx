package org.lorainelab.igb.recentfiles.registry;

import aQute.bnd.annotation.component.Component;
import com.google.common.base.Charsets;
import com.google.common.collect.EvictingQueue;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static java.util.stream.Collectors.toList;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.lorainelab.igb.preferences.PreferenceUtils;
import org.lorainelab.igb.recentfiles.registry.api.RecentFilesRegistry;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component
public class RecentFilesRegistryImpl implements RecentFilesRegistry {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RecentFilesRegistryImpl.class);
    private static final String FILE_NAME = "fileName";
    private static final String TIME_STAMP = "timeStamp";
    private Queue<RecentFileEntry> recentFiles;
    private Preferences modulePreferencesNode;
    private HashFunction md5HashFunction;
    private ObservableList<String> observableRecentFiles;
    private ReadOnlyListWrapper<String> readOnlySetWrapper;
    private static final Comparator<? super RecentFileEntry> name = (o1, o2) -> o1.timeStamp.compareTo(o2.timeStamp);
    private static int oldFileCountLimit = 5;
    
    public RecentFilesRegistryImpl() {
        md5HashFunction = Hashing.md5();
        recentFiles = EvictingQueue.create(oldFileCountLimit);
        modulePreferencesNode = PreferenceUtils.getPackagePrefsNode(RecentFilesRegistryImpl.class);
        initializeFromPreferences();
    }

    private void removeRecentFileFromPreferences(String recentFile) {
        Preferences node = getPreferenceNode(recentFile);
        try {
            node.removeNode();
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void addRecentFileToPreferences(RecentFileEntry recentFile) {
        Preferences node = getPreferenceNode(recentFile.getName());
        node.put(TIME_STAMP, recentFile.timeStamp.toString());
        node.put(FILE_NAME, recentFile.getName());
    }

    private Preferences getPreferenceNode(String recentFile) {
        String nodeName = md5Hash(recentFile);
        Preferences node = modulePreferencesNode.node(nodeName);
        return node;
    }

    private String md5Hash(String filePath) {
        HashCode hc = md5HashFunction.newHasher().putString(filePath, Charsets.UTF_8).hash();
        return hc.toString();
    }

    private void initializeFromPreferences() {
        try {
            Arrays.stream(modulePreferencesNode.childrenNames())
                    .map(nodeName -> modulePreferencesNode.node(nodeName))
                    .forEach(node -> {
                        Optional.ofNullable(node.get(FILE_NAME, null)).ifPresent(recentFile -> {
                            Optional.ofNullable(node.get(TIME_STAMP, null)).ifPresent(timeStampString -> {
                                LocalDateTime timeStamp = LocalDateTime.parse(timeStampString);
                                recentFiles.add(new RecentFileEntry(recentFile, timeStamp));
                            });
                        });
                    });
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }

        final List<String> collect = recentFiles.stream().sorted(name.reversed()).map(entry -> entry.name).collect(toList());
        observableRecentFiles = FXCollections.observableArrayList(collect);
        readOnlySetWrapper = new ReadOnlyListWrapper<>(observableRecentFiles);
    }

    @Override
    public ReadOnlyListWrapper<String> getRecentFiles() {
        return readOnlySetWrapper;
    }

    @Override
    public void addRecentFile(String recentFile) {
        recentFiles.removeIf(file -> file.getName().equals(recentFile));
        if(recentFiles.size() == oldFileCountLimit){
            removeRecentFileFromPreferences(recentFiles.poll().getName());
        }
        RecentFileEntry fileEntry = new RecentFileEntry(recentFile, LocalDateTime.now());
        recentFiles.add(fileEntry);
        addRecentFileToPreferences(fileEntry);
        observableRecentFiles.clear();
        recentFiles.stream().sorted(name.reversed()).map(entry -> entry.name).forEach(observableRecentFiles::add);
    }

    @Override
    public void clearRecentFiles() {
        try {
            recentFiles.clear();
            observableRecentFiles.clear();
            Arrays.stream(modulePreferencesNode.childrenNames())
                    .map(nodeName -> modulePreferencesNode.node(nodeName))
                    .forEach(node -> {
                        try {
                            node.removeNode();
                        } catch (BackingStoreException ex) {
                            LOG.error("Error clearing recent files from preferences", ex);
                        }
                    });
        } catch (BackingStoreException ex) {
            LOG.error("Error clearing recent files from preferences", ex);
        }
    }

    private class RecentFileEntry implements Comparator<RecentFileEntry> {

        private String name;
        private LocalDateTime timeStamp;

        public String getName() {
            return name;
        }

        public LocalDateTime getTimeStamp() {
            return timeStamp;
        }

        public RecentFileEntry(String name, LocalDateTime timeStamp) {
            this.name = name;
            this.timeStamp = timeStamp;
        }

        public void setTimeStamp(LocalDateTime timeStamp) {
            this.timeStamp = timeStamp;
        }

        @Override
        public int compare(RecentFileEntry o1, RecentFileEntry o2) {
            return o1.timeStamp.compareTo(o2.timeStamp);
        }

    }
}
