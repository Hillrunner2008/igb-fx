package org.lorainelab.igb.data.model.util;

import com.google.common.base.Stopwatch;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class StopWatchHelper {

    private static final Logger LOG = LoggerFactory.getLogger(StopWatchHelper.class);
    public static Function<Function<Void, String>, Void> RECORD_METRICS = (Function<Void, String> t) -> {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        String metricName = t.apply(null);
        stopwatch.stop();
        LOG.info("STOPWATCH METRICS for {} {}", metricName, stopwatch);
        return null;
    };
}
