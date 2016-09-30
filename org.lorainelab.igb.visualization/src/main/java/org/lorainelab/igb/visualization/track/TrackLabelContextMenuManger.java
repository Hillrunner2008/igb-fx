package org.lorainelab.igb.visualization.track;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.Set;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.StackPane;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.filehandler.api.DataType;
import org.lorainelab.igb.menu.api.TrackLabelContextMenuEntryProvider;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TrackLabel;
import org.lorainelab.igb.visualization.widget.TrackRenderer;
import org.lorainelab.igb.visualization.widget.ZoomableTrackRenderer;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = TrackLabelContextMenuManger.class)
public class TrackLabelContextMenuManger {

    private CanvasModel canvasModel;
    private Set<TrackLabelContextMenuEntryProvider> labelMenuEntrProviders;

    public TrackLabelContextMenuManger() {
        labelMenuEntrProviders = Sets.newConcurrentHashSet();
    }

    @Reference(optional = true, multiple = true, unbind = "removeLabelMenuEntrProvider", dynamic = true)
    public void addLabelMenuEntrProvider(TrackLabelContextMenuEntryProvider labelMenuEntrProvider) {
        labelMenuEntrProviders.add(labelMenuEntrProvider);
    }

    public void removeLabelMenuEntrProvider(TrackLabelContextMenuEntryProvider labelMenuEntrProvider) {
        labelMenuEntrProviders.remove(labelMenuEntrProvider);
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    public ContextMenu getContextMenu(TrackLabel trackLabel) {
        final ContextMenu contextMenu = new ContextMenu();
        StackPane root = trackLabel.getContent();
        TrackRenderer trackRenderer = trackLabel.getTrackRenderer();
        if (trackRenderer instanceof ZoomableTrackRenderer) {
            ZoomableTrackRenderer zoomableTrackRenderer = (ZoomableTrackRenderer) trackRenderer;
            final Track track = zoomableTrackRenderer.getTrack().orElseThrow(() -> new NullPointerException());//should never be null
            for (TrackLabelContextMenuEntryProvider labelMenuEntrProvider : labelMenuEntrProviders) {
                Set<DataType> dataTypes = track.getDataSet().getFileTypeHandler().getDataTypes();
                Set<DataType> providerSupportedDataTypes = labelMenuEntrProvider.getSupportedDataTypes();
                Set<WeightedMenuEntry> toAdd = Sets.newTreeSet(Comparator.comparingInt(wme -> wme.getWeight()));
                if (dataTypes.contains(DataType.ANNOTATION) || providerSupportedDataTypes.contains(DataType.ANNOTATION)) {
                    labelMenuEntrProvider.getMenuItems(track, canvasModel.getRefreshAction()).ifPresent(toAdd::addAll);
                }
                if (dataTypes.contains(DataType.ALIGNMENT)) {
                    labelMenuEntrProvider.getMenuItems(track, canvasModel.getRefreshAction()).ifPresent(toAdd::addAll);
                }
                if (dataTypes.contains(DataType.GRAPH)) {
                    labelMenuEntrProvider.getMenuItems(track, canvasModel.getRefreshAction()).ifPresent(toAdd::addAll);
                }
                for (WeightedMenuEntry wme : toAdd) {
                    contextMenu.getItems().add(wme.getMenuEntry());
                }

            }
        }
        return contextMenu;
    }
}
