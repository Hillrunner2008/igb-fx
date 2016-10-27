package org.broad.igv.bbfile;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import org.lorainelab.igb.data.model.chart.ChartData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class MemUsageTest {

    private static final Logger LOG = LoggerFactory.getLogger(MemUsageTest.class);

    @Test
    public void testCube() throws IOException {

        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used Memory before " + usedMemoryBefore);
        // working code here

        IntArrayList x;
        IntArrayList w = new IntArrayList();
        DoubleArrayList y;
        x = new IntArrayList();
        y = new DoubleArrayList();
        BBFileReader bbReader = new BBFileReader("/home/dcnorris/Downloads/cold_treatment.sm.bw");
        BBFileHeader bbFileHdr = bbReader.getBBFileHeader();
        if (bbFileHdr.isBigWig()) {
            BigWigIterator bigWigIterator = bbReader.getBigWigIterator("chr1", 0, "chr1", 30_427_671, true);
            try {
                WigItem wigItem = null;
                while (bigWigIterator.hasNext()) {
                    wigItem = bigWigIterator.next();
                    if (wigItem == null) {
                        break;
                    }
                    final int minX = wigItem.getStartBase();
                    final int maxX = wigItem.getEndBase();
                    final float wigValue = wigItem.getWigValue();
                    x.add(minX);
                    w.add(maxX - minX);
                    y.add(wigValue);
                }
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
        int dataSize = x.size();
        int[] xData = Arrays.copyOf(x.elements(), dataSize);
        int[] wData = Arrays.copyOf(w.elements(), dataSize);
        double[] yData = Arrays.copyOf(y.elements(), dataSize);
        y = null;
        w = null;
        x = null;
        ChartData cd = new ChartData(xData, wData, yData);
        System.gc();
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory increased:" + (usedMemoryAfter - usedMemoryBefore));

    }
}
