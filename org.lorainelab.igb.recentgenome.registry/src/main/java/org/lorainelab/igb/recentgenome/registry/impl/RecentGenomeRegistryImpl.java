/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.recentgenome.registry.impl;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.base.Charsets;
import com.google.common.collect.EvictingQueue;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static java.util.stream.Collectors.toList;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.preferences.PreferenceUtils;
import org.lorainelab.igb.recentgenome.registry.RecentGenomeRegistry;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta
 */
@Component
public class RecentGenomeRegistryImpl implements RecentGenomeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(RecentGenomeRegistryImpl.class);
    private static final String FILE_NAME = "fileName";
    private static final String TIME_STAMP = "timeStamp";
    private Queue<RecentGenomeEntry> recentFiles;
    private Preferences modulePreferencesNode;
    private final HashFunction md5HashFunction;
    private ObservableList<GenomeVersion> observableRecentFiles;
    private ReadOnlyListWrapper<GenomeVersion> readOnlyListWrapper;
    private static final Comparator<? super RecentGenomeEntry> comparator = (o1, o2) -> o1.timeStamp.compareTo(o2.timeStamp);
    private static final int oldFileCountLimit = 5;
    private GenomeVersionRegistry genomeVersionRegistry;
    private SelectionInfoService selectionInfoService;

    public RecentGenomeRegistryImpl() {
        md5HashFunction = Hashing.md5();
        recentFiles = EvictingQueue.create(oldFileCountLimit);
        modulePreferencesNode = PreferenceUtils.getPackagePrefsNode(RecentGenomeRegistryImpl.class);
    }

    @Activate
    public void activate() {
        initializeFromPreferences();
        //Add if non-custom genomes are to be stored in recent
        selectionInfoService.getSelectedGenomeVersion().addListener((ObservableValue<? extends Optional<GenomeVersion>> observable, Optional<GenomeVersion> oldValue, Optional<GenomeVersion> newValue) -> {
            newValue.ifPresent(newGenome -> addRecentGenome(newGenome));
        });
        
        genomeVersionRegistry.getRegisteredGenomeVersions().addListener((SetChangeListener.Change<? extends GenomeVersion> change) -> {
            if (change.wasRemoved()) {
                    removeRecentFileFromPreferences(change.getElementRemoved());
                    initializeFromPreferences();
                }
        });
    }

    private void removeRecentFileFromPreferences(GenomeVersion recentFile) {
        Preferences node = getPreferenceNode(recentFile.getReferenceSequenceProvider().getPath());
        try {
            node.removeNode();
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void removeRecentFileFromPreferences(Preferences prefNode) {
        try {
            prefNode.removeNode();
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void addRecentFileToPreferences(RecentGenomeEntry recentFile) {
        Preferences node = getPreferenceNode(recentFile.getGenome().getReferenceSequenceProvider().getPath());
        node.put(TIME_STAMP, recentFile.timeStamp.toString());
        node.put(FILE_NAME, recentFile.getGenome().getReferenceSequenceProvider().getPath());
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
                                Optional<GenomeVersion> optional = genomeVersionRegistry.getRegisteredGenomeVersions().stream().filter(gv -> gv.getReferenceSequenceProvider().getPath().equals(recentFile)).findFirst();
                                if (!optional.isPresent()) {
                                    //handle if genome not loaded
                                } else {
                                    recentFiles.add(new RecentGenomeEntry(optional.get(), timeStamp));
                                }
                            });
                        });
                    });
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }

        final List<GenomeVersion> collect = recentFiles.stream().sorted(comparator.reversed()).map(entry -> entry.genome).collect(toList());
        observableRecentFiles = FXCollections.observableArrayList(collect);
        readOnlyListWrapper = new ReadOnlyListWrapper<>(observableRecentFiles);
    }

    @Override
    public ReadOnlyListWrapper<GenomeVersion> getRecentGenomes() {
        return readOnlyListWrapper;
    }

    @Override
    public void addRecentGenome(GenomeVersion recentFile) {
        recentFiles.removeIf(file -> file.getGenome().equals(recentFile));
        if (recentFiles.size() == oldFileCountLimit) {
            removeRecentFileFromPreferences(recentFiles.poll().getGenome());
        }
        RecentGenomeEntry fileEntry = new RecentGenomeEntry(recentFile, LocalDateTime.now());
        recentFiles.add(fileEntry);
        addRecentFileToPreferences(fileEntry);
        observableRecentFiles.clear();
        recentFiles.stream().sorted(comparator.reversed()).map(entry -> entry.genome).forEach(observableRecentFiles::add);
    }

    @Override
    public void clearRecentGenomes() {
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

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    private class RecentGenomeEntry implements Comparator<RecentGenomeEntry> {

        private GenomeVersion genome;
        private LocalDateTime timeStamp;

        public GenomeVersion getGenome() {
            return genome;
        }

        public LocalDateTime getTimeStamp() {
            return timeStamp;
        }

        public RecentGenomeEntry(GenomeVersion name, LocalDateTime timeStamp) {
            this.genome = name;
            this.timeStamp = timeStamp;
        }

        public void setTimeStamp(LocalDateTime timeStamp) {
            this.timeStamp = timeStamp;
        }

        @Override
        public int compare(RecentGenomeEntry o1, RecentGenomeEntry o2) {
            return o1.timeStamp.compareTo(o2.timeStamp);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + Objects.hashCode(this.genome);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RecentGenomeEntry other = (RecentGenomeEntry) obj;
            return genome.equals(other.genome);
        }

    }

}
