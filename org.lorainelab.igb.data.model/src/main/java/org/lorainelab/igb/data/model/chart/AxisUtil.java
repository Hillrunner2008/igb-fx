package org.lorainelab.igb.data.model.chart;

/**
 *
 * @author dcnorris
 */
public class AxisUtil {

    public static double getMajorTick(double value) {
        int approxIntervals = 10;
        int incr1 = 5;
        int incr2 = 10;
        int incr3 = 25;
        int lastSmallestIncr = 0;
        double lastSmallest = value;
        while (lastSmallest > approxIntervals) {
            double value1 = value / incr1;
            double value2 = value / incr2;
            double value3 = value / incr3;
            if (value1 > approxIntervals && value2 > approxIntervals && value3 > approxIntervals) {
                lastSmallest = findSmallest(new double[]{value1, value2, value3});
                if (lastSmallest == value1) {
                    lastSmallestIncr = incr1;
                } else if (lastSmallest == value2) {
                    lastSmallestIncr = incr2;
                } else {
                    lastSmallestIncr = incr3;
                }

                incr1 *= 10;
                incr2 *= 10;
                incr3 *= 10;
            } else {

                double value1Diff = Math.abs(approxIntervals - value1);
                double value2Diff = Math.abs(approxIntervals - value2);
                double value3Diff = Math.abs(approxIntervals - value3);
                double lastSmallestDiff = Math.abs(approxIntervals - lastSmallest);
                double smallestDiff = findSmallest(new double[]{value1Diff, value2Diff, value3Diff, lastSmallestDiff});
                if (smallestDiff == value1Diff) {
                    return incr1;
                } else if (smallestDiff == value2Diff) {
                    return incr2;
                } else if (smallestDiff == value3Diff) {
                    return incr3;
                } else {
                    return lastSmallestIncr;
                }
            }
        }
        return 10;
    }

    private static double findSmallest(double[] numbers) {
        double smallest = Double.MAX_VALUE;

        for (int i = 0; i < numbers.length; i++) {
            if (smallest > numbers[i]) {
                smallest = numbers[i];
            }
        }
        return smallest;
    }
}
