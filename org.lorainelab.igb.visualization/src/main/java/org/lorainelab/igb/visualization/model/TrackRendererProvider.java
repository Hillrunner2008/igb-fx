package org.lorainelab.igb.visualization.model;

import java.util.Set;

/**
 *
 * @author dcnorris
 */
public interface TrackRendererProvider {

    int getModelWidth();

    Set<TrackRenderer> getTrackRenderers();

    Track getNegativeStrandTrack();

    Track getPositiveStrandTrack();
}
