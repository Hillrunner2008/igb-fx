package org.lorainelab.igb.data.model.util;

import com.google.common.collect.Range;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.tribble.readers.TabixIteratorLineReader;
import htsjdk.tribble.readers.TabixReader;
import org.lorainelab.igb.data.model.Chromosome;
import static org.lorainelab.igb.data.model.util.DataSourceUtilsImpl.getStreamFor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class TabixUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TabixUtils.class);

    public static LineIteratorImpl getLineIterator(String dataSourceReference, Range<Integer> range, Chromosome chromosome) {
        LineIteratorImpl iterator = null;
        try {
            try {
                final TabixReader tabixReader = new TabixReader(dataSourceReference);
                final TabixReader.Iterator queryResults = tabixReader.query(chromosome.getName(), range.lowerEndpoint(), range.upperEndpoint());
                iterator = new LineIteratorImpl(new TabixIteratorLineReader(queryResults));
            } catch (Throwable ex) {
                iterator = new LineIteratorImpl(new AsciiLineReader(getStreamFor(dataSourceReference)));
            }
        } catch (Throwable ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return iterator;
    }
}
