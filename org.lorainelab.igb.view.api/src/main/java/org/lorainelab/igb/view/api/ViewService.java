package org.lorainelab.igb.view.api;

import com.google.common.collect.Range;
import java.util.Optional;

/**
 *
 * @author dcnorris
 */
public interface ViewService {

    public Runnable getRefreshViewAction();

    public Optional<Range<Double>> getViewCoordinates();
}
