package org.lorainelab.igb.menu.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.filehandler.api.DataType;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;

/**
 *
 * @author dcnorris
 */
public interface TrackLabelContextMenuEntryProvider {

    Set<DataType> getSupportedDataTypes();

    Optional<List<WeightedMenuEntry>> getMenuItems(Track track, Runnable refreshAction);
}
