package org.lorainelab.igb.data.model;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.util.Arrays;
import org.lorainelab.igb.data.model.sequence.ReferenceSequenceProvider;
import org.lorainelab.igb.data.model.util.AlphanumComparator;

/**
 *
 * @author dcnorris
 */
public class Chromosome implements Comparable<Chromosome> {

    static final AlphanumComparator ALPHANUM_COMPARATOR = new AlphanumComparator();
    private String name;
    private int length;
    private RangeSet<Integer> loadedRegions;
    private char[] sequence;
    private final ReferenceSequenceProvider referenceSequenceProvider;

    public Chromosome(String name, int length, ReferenceSequenceProvider referenceSequenceProvider) {
        this.name = name;
        this.length = length;
        this.referenceSequenceProvider = referenceSequenceProvider;
        loadedRegions = TreeRangeSet.create();
    }

    public ReferenceSequenceProvider getReferenceSequenceProvider() {
        return referenceSequenceProvider;
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
        if (sequence == null) {
            char[] filler = new char[endPos - startPos];
            Arrays.fill(filler, (char) '-');
            return filler;
        }
        return Arrays.copyOfRange(sequence, startPos, endPos);
    }

    public void loadRegion(Range<Integer> requestRange) {
        if (sequence == null) {
            sequence = new char[length];
            Arrays.fill(sequence, (char) '-');
        }
        if (!loadedRegions.encloses(requestRange)) {
            RangeSet<Integer> connectedRanges = loadedRegions.subRangeSet(requestRange);
            TreeRangeSet<Integer> updatedRequestRange = TreeRangeSet.create(connectedRanges);
            updatedRequestRange.add(requestRange);
            loadedRegions.add(updatedRequestRange.span());
            final int requestLength = updatedRequestRange.span().upperEndpoint() - updatedRequestRange.span().lowerEndpoint();
            String requestedSequence = referenceSequenceProvider.getSequence(name, updatedRequestRange.span().lowerEndpoint(), requestLength);
            for (int i = 0; i < requestLength; i++) {
                int index = updatedRequestRange.span().lowerEndpoint() + i;
                sequence[index] = requestedSequence.charAt(i);
            }
        }
    }

    @Override
    public int compareTo(Chromosome o) {
        return ALPHANUM_COMPARATOR.compare(name, o.getName());
    }

}
