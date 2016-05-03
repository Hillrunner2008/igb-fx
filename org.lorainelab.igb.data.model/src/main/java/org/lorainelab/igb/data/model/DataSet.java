package org.lorainelab.igb.data.model;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.TreeMultimap;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import static org.lorainelab.igb.data.model.glyph.Glyph.MIN_X_COMPARATOR;

/**
 *
 * @author dcnorris
 */
public class DataSet {

    private final Multimap<String, CompositionGlyph> loadedAnnoations;
    private final String trackLabel;
    private final Multimap<String, Range<Integer>> loadedRegions;
    //tracked to prevent duplicate requests
    private final Multimap<String, Range<Integer>> activeLoadRequests;
    private final FileTypeHandler fileTypeHandler;
    private final DataSourceReference dataSourceReference;
    private Track positiveStrandTrack;
    private Track combinedStrandTrack;
    private Track negativeStrandTrack;

    public DataSet(String trackLabel, DataSourceReference dataSourceReference, FileTypeHandler fileTypeHandler) {
        loadedAnnoations = TreeMultimap.create(Ordering.natural(), MIN_X_COMPARATOR);
        this.dataSourceReference = dataSourceReference;
        this.trackLabel = trackLabel;
        this.fileTypeHandler = fileTypeHandler;
        loadedRegions = TreeMultimap.create(Ordering.natural(), Comparator.comparingInt(range -> range.lowerEndpoint()));
        activeLoadRequests = TreeMultimap.create(Ordering.natural(), Comparator.comparingInt(range -> range.lowerEndpoint()));
        positiveStrandTrack = new Track(true, trackLabel + " (+)", 10);
        negativeStrandTrack = new Track(true, trackLabel + " (-)", 10);
        combinedStrandTrack = new Track(true, trackLabel + " (+/-)", 10);
    }

    public Track getPositiveStrandTrack(String chrId) {
        refreshPositiveStrandTrack(chrId);
        return positiveStrandTrack;
    }

    private void refreshPositiveStrandTrack(String chrId) {
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

    public void loadRegion(String chrId, Range<Integer> range) {
        activeLoadRequests.put(chrId, range);
        loadedAnnoations.putAll(chrId, fileTypeHandler.getRegion(dataSourceReference, range, chrId));
        refreshPositiveStrandTrack(chrId);
        refreshNegativeStrandTrack(chrId);
        refreshCombinedTrack(chrId);
    }

}
