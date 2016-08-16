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
    //tracked to prevent duplicate requests
    private final FileTypeHandler fileTypeHandler;
    private final DataSourceReference dataSourceReference;
    private Track positiveStrandTrack;
    private Track combinedStrandTrack;
    private Track negativeStrandTrack;

    public DataSet(String trackLabel, DataSourceReference dataSourceReference, FileTypeHandler fileTypeHandler) {
        loadedAnnoations = HashMultimap.create();
        this.dataSourceReference = dataSourceReference;
        this.trackLabel = trackLabel;
        this.fileTypeHandler = fileTypeHandler;
        loadedRegions = Maps.newHashMap();
        positiveStrandTrack = new Track(false, trackLabel + " (+)", DEFAULT_STACK_HEIGHT);
        negativeStrandTrack = new Track(true, trackLabel + " (-)", DEFAULT_STACK_HEIGHT);
        combinedStrandTrack = new Track(true, trackLabel + " (+/-)", DEFAULT_STACK_HEIGHT);
    }

    public Track getPositiveStrandTrack(String chrId) {
        refreshPositiveStrandTrack(chrId);
        return positiveStrandTrack;
    }

    private void refreshPositiveStrandTrack(String chrId) {
        positiveStrandTrack.clearGlyphs();
        positiveStrandTrack.getGlyphs().addAll(
                loadedAnnoations.get(chrId).stream()
                        .filter(g -> g.getTooltipData().get("forward").equals("true"))
                        .collect(Collectors.toList())
        );
        positiveStrandTrack.buildSlots();
    }

    public Track getNegativeStrandTrack(String chrId) {
        refreshNegativeStrandTrack(chrId);
        return negativeStrandTrack;
    }

    private void refreshNegativeStrandTrack(String chrId) {
        negativeStrandTrack.clearGlyphs();
        negativeStrandTrack.getGlyphs().addAll(
                loadedAnnoations.get(chrId).stream()
                        .filter(g -> g.getTooltipData().get("forward").equals("false"))
                        .collect(Collectors.toList()));
        negativeStrandTrack.buildSlots();
    }

    public Track getCombinedStrandTrack(String chrId, int stackHeight) {
        refreshCombinedTrack(chrId);
        return combinedStrandTrack;
    }

    private void refreshCombinedTrack(String chrId) {
//        combinedStrandTrack.getGlyphs().addAll(loadedAnnoations.get(chrId));
//        combinedStrandTrack.buildSlots();
    }

    public void loadRegion(String chrId, Range<Integer> requestRange) {
        if (!loadedRegions.containsKey(chrId)) {
            loadedRegions.put(chrId, TreeRangeSet.create());
        }
        if (!loadedRegions.get(chrId).encloses(requestRange)) {
            RangeSet<Integer> connectedRanges = loadedRegions.get(chrId).subRangeSet(requestRange);
            TreeRangeSet<Integer> updatedRequestRange = TreeRangeSet.create(connectedRanges);
            updatedRequestRange.add(requestRange);
            loadedRegions.get(chrId).add(updatedRequestRange.span());
            loadedAnnoations.putAll(chrId, fileTypeHandler.getRegion(dataSourceReference, updatedRequestRange.span(), chrId));
            refreshPositiveStrandTrack(chrId);
            refreshNegativeStrandTrack(chrId);
            refreshCombinedTrack(chrId);
        }
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

}
