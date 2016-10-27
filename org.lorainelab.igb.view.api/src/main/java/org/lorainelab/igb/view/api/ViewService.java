package org.lorainelab.igb.view.api;

import com.google.common.collect.Range;
import java.util.Optional;

/**
 *
 * @author dcnorris
 */
public interface ViewService {

    Runnable getRefreshViewAction();

    Optional<Range<Double>> getViewCoordinates();

    void setViewCoordinateRange(Range<Double> range);

}
