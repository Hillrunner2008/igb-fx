package org.lorainelab.igb.data.model;

import com.google.common.collect.Range;
import java.util.Arrays;

/**
 *
 * @author dcnorris
 */
public class Chromosome {

    private String name;
    private int length;
    private char[] sequence;
    private final ReferenceSequenceProvider referenceSequenceProvider;

    public Chromosome(String name, int length, ReferenceSequenceProvider referenceSequenceProvider) {
        this.name = name;
        this.length = length;
        sequence = new char[length];
        Arrays.fill(sequence, (char) '-');
        this.referenceSequenceProvider = referenceSequenceProvider;
    }

    public void setLastName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public char[] getSequence(int start, int length) {
        final int startPos = Math.max(0, start);    // qstart should never be < 0
        final int endPos = Math.min(startPos + length, this.length);
        final Range<Integer> requestRange = Range.closedOpen(startPos, endPos);
        if (isLoadedRegion(requestRange)) {
            return Arrays.copyOfRange(sequence, startPos, endPos);
        } else {
            loadRegion(requestRange);
            return Arrays.copyOfRange(sequence, startPos, endPos);
        }
    }

    //TODO implement
    private boolean isLoadedRegion(Range<Integer> requestRange) {
        return true;
    }

    //TODO implement
    private void loadRegion(Range<Integer> requestRange) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
