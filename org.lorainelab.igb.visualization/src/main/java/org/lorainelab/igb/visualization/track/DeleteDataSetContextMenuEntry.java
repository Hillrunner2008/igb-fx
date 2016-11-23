package org.lorainelab.igb.visualization.track;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.filehandler.api.DataType;
import org.lorainelab.igb.menu.api.TrackLabelContextMenuEntryProvider;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.selections.SelectionInfoService;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class DeleteDataSetContextMenuEntry implements TrackLabelContextMenuEntryProvider {

    private SelectionInfoService selectionInfoService;

    @Override
    public Set<DataType> getSupportedDataTypes() {
        return Sets.newHashSet(DataType.ANNOTATION, DataType.ALIGNMENT, DataType.GRAPH);
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems(Track track, Runnable refreshAction) {
        WeightedMenuItem deleteMenuItem = new WeightedMenuItem(0);
        deleteMenuItem.setText("Delete dataset");

        deleteMenuItem.setOnAction(evt -> {
            DataSet dataSet = track.getDataSet();
            dataSet.clearData();
            selectionInfoService.getSelectedGenomeVersion().get().ifPresent(selectedGenomeVersion -> {
                selectedGenomeVersion.getLoadedDataSets().remove(dataSet);
            });
        });

        return Optional.ofNullable(Lists.newArrayList(deleteMenuItem));
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

}
