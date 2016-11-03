package org.lorainelab.igb.data.model;

import cern.colt.list.IntArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class PrimTest {

    private static final Logger LOG = LoggerFactory.getLogger(PrimTest.class);

//    @Test
    public void validateRangeQueryIndexSearch() {
        int xCoords[] = new int[]{1, 5, 7, 10};
        Assert.assertEquals(determineBegIndex(8, xCoords), 2);
        Assert.assertEquals(determineEndIndex(10, xCoords), 3);
    }

    @Test
    public void validateRangeQuery() {
        int xCoords[] = new int[]{1, 5, 7, 10};
        final IntArrayList keys = new IntArrayList(xCoords);
        int index = keys.binarySearch(8);
        if (index >= 0) {
            Assert.assertEquals(index, 2);
        } else {
            Assert.assertEquals(Math.max(0, (-index - 2)), 2);
        }

        int endIndex = keys.binarySearch(10);
        if (endIndex >= 0) {
            Assert.assertEquals(endIndex, 3);
        } else {
            // negative, which means it's (-(first elt > key) - 1).
            // We want that first elt.
            endIndex = -endIndex - 1;

            // need to be sure that this doesn't go beyond the end of the array, if all points are less than xmax
            endIndex = Math.min(endIndex, xCoords.length - 1);
            // need to be sure it's not less than 0
            endIndex = Math.max(0, endIndex);
            Assert.assertEquals(endIndex, 3);
        }

    }

//    Find last point with value <= xmin.
    public static int determineBegIndex(double xmin, int xCoords[]) {
        int index = Arrays.binarySearch(xCoords, (int) Math.floor(xmin));
        if (index >= 0) {
            return index;
        }
        // negative, which means it's (-(first elt > key) - 1).
        // Thus first elt <= key = (-index - 1) -1 = (-index -2)
        return Math.max(0, (-index - 2));
    }

    /**
     * Find first point with value >= xmax. Use previous starting index as a
     * starting point.
     */
    public final int determineEndIndex(double xmax, int xCoords[]) {
        int index = Arrays.binarySearch(xCoords, (int) Math.ceil(xmax));
        if (index >= 0) {
            return index;
        }
        // negative, which means it's (-(first elt > key) - 1).
        // We want that first elt.
        index = -index - 1;

        // need to be sure that this doesn't go beyond the end of the array, if all points are less than xmax
        index = Math.min(index, xCoords.length - 1);
        // need to be sure it's not less than 0
        index = Math.max(0, index);

        return index;

    }

}
