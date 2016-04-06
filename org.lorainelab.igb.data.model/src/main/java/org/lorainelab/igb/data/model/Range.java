/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model;

import java.util.Optional;

/**
 *
 * @author dcnorris
 */
public class Range {

    private int start;
    private int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return getEnd() - getStart();
    }

    public boolean isOverlapping(Range feature) {
        com.google.common.collect.Range<Integer> featureRange = com.google.common.collect.Range
                .closed(feature.getStart(), feature.getEnd());
        com.google.common.collect.Range<Integer> range = com.google.common.collect.Range
                .closed(getStart(), getEnd());
        return range.isConnected(featureRange);
    }

    /**
     * Returns the maximal range  enclosed by both this range and
     * connectedRange, if such a range exists.
     *
     * <p>For example, the intersection of {@code [1..5]} and {@code (3..7)} is {@code (3..5]}. The
     * resulting range may be empty; for example, {@code [1..5)} intersected with {@code [5..7)}
     * yields the empty range {@code [5..5)}.
     *
     * <p>The intersection exists if and only if the two ranges are {@linkplain #isConnected
     * connected}.
     *
     * <p>The intersection operation is commutative, associative and idempotent, and its identity
     * element is {@link Range#all}).
     *
     * @throws IllegalArgumentException if {@code isConnected(connectedRange)} is {@code false}
     */
    public Optional<Range> getIntersection(Range otherRange) {
        if (!isOverlapping(otherRange)) {
            return Optional.empty();
        }
        com.google.common.collect.Range<Integer> featureRange = com.google.common.collect.Range
                .closed(otherRange.getStart(), otherRange.getEnd());
        com.google.common.collect.Range<Integer> range = com.google.common.collect.Range
                .closed(getStart(), getEnd());
        com.google.common.collect.Range<Integer> intersection = range.intersection(featureRange);
        return Optional.of(new Range(intersection.lowerEndpoint(),
                intersection.upperEndpoint()
        ));
    }
}
