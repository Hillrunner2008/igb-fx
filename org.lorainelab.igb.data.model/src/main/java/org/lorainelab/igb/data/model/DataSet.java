package org.lorainelab.igb.data.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.lorainelab.igb.data.model.filehandler.api.DataType;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;

/**
 *
 * @author dcnorris
 */
public class DataSet {
    
    private static final int DEFAULT_STACK_HEIGHT = 5;
    private final HashMultimap<String, CompositionGlyph> loadedAnnoations;
    private final String trackLabel;
    private Map<String, RangeSet<Integer>> loadedRegions;
    private final FileTypeHandler fileTypeHandler;
    private final DataSourceReference dataSourceReference;
    private StackedGlyphTrack positiveStrandTrack;
    private StackedGlyphTrack combinedStrandTrack;
    private StackedGlyphTrack negativeStrandTrack;
    private GraphTrack graphTrack;
    
    public DataSet(String trackLabel, DataSourceReference dataSourceReference, FileTypeHandler fileTypeHandler) {
        loadedAnnoations = HashMultimap.create();
        this.dataSourceReference = dataSourceReference;
        this.trackLabel = trackLabel;
        this.fileTypeHandler = fileTypeHandler;
        loadedRegions = Maps.newHashMap();
        if (isGraphType()) {
            graphTrack = new GraphTrack(trackLabel, this);
        } else {
            positiveStrandTrack = new StackedGlyphTrack(false, trackLabel + " (+)", DEFAULT_STACK_HEIGHT, this);
            negativeStrandTrack = new StackedGlyphTrack(true, trackLabel + " (-)", DEFAULT_STACK_HEIGHT, this);
            combinedStrandTrack = new StackedGlyphTrack(true, trackLabel + " (+/-)", DEFAULT_STACK_HEIGHT, this);
        }
    }
    
    public boolean isGraphType() {
        return fileTypeHandler.getDataTypes().contains(DataType.GRAPH);
    }
    
    public Track getPositiveStrandTrack(String chrId) {
        refreshPositiveStrandTrack(chrId);
        return positiveStrandTrack;
    }
    
    private void refreshGraphTrack(String chrId) {
        graphTrack.clearGlyphs();
        graphTrack.addGlyphs(loadedAnnoations.get(chrId));
    }
    
    private void refreshPositiveStrandTrack(String chrId) {
        positiveStrandTrack.clearGlyphs();
        positiveStrandTrack.addGlyphs(
                loadedAnnoations.get(chrId).stream()
                        .filter(g -> !g.isNegative())
                        .collect(Collectors.toList())
        );
    }
    
    public StackedGlyphTrack getNegativeStrandTrack(String chrId) {
        refreshNegativeStrandTrack(chrId);
        return negativeStrandTrack;
    }
    
    private void refreshNegativeStrandTrack(String chrId) {
        negativeStrandTrack.clearGlyphs();
        negativeStrandTrack.addGlyphs(
                loadedAnnoations.get(chrId).stream()
                        .filter(g -> g.isNegative())
                        .collect(Collectors.toList()));
    }
    
    public StackedGlyphTrack getCombinedStrandTrack(String chrId, int stackHeight) {
        refreshCombinedTrack(chrId);
        return combinedStrandTrack;
    }

    public FileTypeHandler getFileTypeHandler() {
        return fileTypeHandler;
    }
    
    private void refreshCombinedTrack(String chrId) {
//        combinedStrandTrack.getGlyphs().addAll(loadedAnnoations.get(chrId));
    }
    
    public void loadRegion(String chrId, Range<Integer> requestRange) {
        RangeSet<Integer> loadedChromosomeRegions = loadedRegions.computeIfAbsent(chrId, e -> {
            return TreeRangeSet.create();
        });
        if (!loadedChromosomeRegions.encloses(requestRange)) {
            RangeSet<Integer> connectedRanges = loadedRegions.get(chrId).subRangeSet(requestRange);
            TreeRangeSet<Integer> updatedRequestRange = TreeRangeSet.create(connectedRanges);
            updatedRequestRange.add(requestRange);
            loadedRegions.get(chrId).add(updatedRequestRange.span());
            loadedAnnoations.putAll(chrId, fileTypeHandler.getRegion(dataSourceReference, updatedRequestRange.span(), chrId));
            if (isGraphType()) {
                refreshGraphTrack(chrId);
            } else {
                refreshPositiveStrandTrack(chrId);
                refreshNegativeStrandTrack(chrId);
                refreshCombinedTrack(chrId);
            }
        }
    }
    
    public RangeSet<Integer> getLoadedRegions(String chr) {
        return loadedRegions.getOrDefault(chr, TreeRangeSet.create());
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.trackLabel);
        hash = 37 * hash + Objects.hashCode(this.fileTypeHandler);
        hash = 37 * hash + Objects.hashCode(this.dataSourceReference);
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
        final DataSet other = (DataSet) obj;
        if (!Objects.equals(this.trackLabel, other.trackLabel)) {
            return false;
        }
        if (!Objects.equals(this.fileTypeHandler, other.fileTypeHandler)) {
            return false;
        }
        if (!Objects.equals(this.dataSourceReference, other.dataSourceReference)) {
            return false;
        }
        return true;
    }
    
    public Track getGraphTrack() {
        return graphTrack;
    }
    
}
