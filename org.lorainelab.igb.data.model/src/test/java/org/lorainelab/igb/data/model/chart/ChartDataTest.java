/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.chart;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import com.vividsolutions.jts.geom.Coordinate;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author dcnorris
 */
public class ChartDataTest {

    private IntervalChart chart;
    IntArrayList x;
    IntArrayList w;
    DoubleArrayList y;

    @Before
    public void setup() {
        double[] yValues = new double[]{10, 25, 55, 75};
        x = new IntArrayList();
        w = new IntArrayList();
        y = new DoubleArrayList();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final double yValue = yValues[random.nextInt(3)];
            x.add(i);
            w.add(5);
            y.add(yValue);
        }
        int dataSize = x.size();
        int[] xData = Arrays.copyOf(x.elements(), dataSize);
        int[] wData = Arrays.copyOf(w.elements(), dataSize);
        double[] yData = Arrays.copyOf(y.elements(), dataSize);
        chart = new IntervalChart(xData, wData, yData);
    }

    @Test
    public void testDataInRange() {
        List<Coordinate> t = chart.getDataInRange(new Rectangle2D.Double(3, 0, 3, 100), 1);
        Assert.assertEquals(4, t.size());
        Assert.assertEquals(t.get(0).y, y.get(3), 0);
        Assert.assertEquals(t.get(1).y, y.get(4), 0);
        Assert.assertEquals(t.get(2).y, y.get(5), 0);
        Assert.assertEquals(t.get(3).y, y.get(6), 0);
    }

    @Test
    public void testLargeRange() {
        List<Coordinate> t = chart.getDataInRange(new Rectangle2D.Double(0, 0, 30_000_000, 68_000), 30_000_000 / 800);
        Assert.assertEquals(1, t.size());
    }

}
