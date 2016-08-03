package org.lorainelab.igb.visualization.model;

import aQute.bnd.annotation.component.Component;
import java.util.Set;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = TracksModel.class)
public class TracksModel {

    private double totalTrackHeight;
    private Set<TrackRenderer> trackRenderers;
}
