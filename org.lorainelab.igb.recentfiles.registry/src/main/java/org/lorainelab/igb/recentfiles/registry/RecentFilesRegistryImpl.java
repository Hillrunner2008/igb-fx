package org.lorainelab.igb.recentfiles.registry;

import aQute.bnd.annotation.component.Component;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
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
    private ObservableSet<String> recentFiles;
    private Preferences modulePreferencesNode;
    private HashFunction md5HashFunction;

    public RecentFilesRegistryImpl() {
        md5HashFunction = Hashing.md5();
        recentFiles = FXCollections.observableSet(Sets.newHashSet());
        modulePreferencesNode = PreferenceUtils.getPackagePrefsNode(RecentFilesMenuEntry.class);
        initializeFromPreferences();
        initializeRecentFilesChangeListener();
    }

    private void initializeRecentFilesChangeListener() {
        recentFiles.addListener((SetChangeListener.Change<? extends String> change) -> {
            if (change.wasAdded()) {
                addRecentFileToPreferences(change.getElementAdded(),LocalDateTime.now());
            } else if (change.wasRemoved()) {
                removeRecentFileFromPreferences(change.getElementRemoved());
            }
        });
    }

    private void removeRecentFileFromPreferences(String recentFile) {
        Preferences node = getPreferenceNode(recentFile);
        try {
            node.removeNode();
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void addRecentFileToPreferences(String recentFile,LocalDateTime timeStamp ) {
        Preferences node = getPreferenceNode(recentFile);
        node.put(TIME_STAMP, timeStamp.toString());
        node.put(FILE_NAME, recentFile);
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

    @Override
    public ObservableSet<String> getRecentFiles() {
        return recentFiles;
    }

    private void initializeFromPreferences() {
        try {
            Arrays.stream(modulePreferencesNode.childrenNames())
                    .map(nodeName -> modulePreferencesNode.node(nodeName))
                    .forEach(node -> {
                        Optional.ofNullable(node.get(FILE_NAME, null)).ifPresent(recentFiles::add);
                    });
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }

    }

}
