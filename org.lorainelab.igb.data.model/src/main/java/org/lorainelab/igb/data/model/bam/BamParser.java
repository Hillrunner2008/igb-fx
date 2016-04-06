package org.lorainelab.igb.data.model.bam;

import htsjdk.samtools.BAMFileSpan;
import htsjdk.samtools.DiskBasedBAMFileIndex;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.seekablestream.SeekableBufferedStream;
import htsjdk.samtools.seekablestream.SeekableFileStream;
import htsjdk.samtools.util.CloserUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.lorainelab.igb.data.model.bed.BedParser;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class BamParser {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BamParser.class);
    private List<BamFeature> annotations;

    public BamParser() {
        annotations = new ArrayList<>();
        parse();
    }

    private void parse() {
        try {
            SeekableBufferedStream bamSeekableStream = new SeekableBufferedStream(new SeekableFileStream(new File(BedParser.class.getClassLoader().getResource("small.bam").getFile())));
            SeekableBufferedStream indexSeekableStream = new SeekableBufferedStream(new SeekableFileStream(new File(BedParser.class.getClassLoader().getResource("small.bam.bai").getFile())));
            final SAMSequenceDictionary samSequenceDictionary = new SAMSequenceDictionary();
            samSequenceDictionary.addSequence(new SAMSequenceRecord("chr1", 30427671));
            final DiskBasedBAMFileIndex bamIndex = new DiskBasedBAMFileIndex(indexSeekableStream, samSequenceDictionary);
            SamReader reader = SamReaderFactory.make().validationStringency(ValidationStringency.SILENT).open(SamInputResource.of(bamSeekableStream));
            BAMFileSpan spanOverlapping = bamIndex.getSpanOverlapping(samSequenceDictionary.getSequenceIndex("chr1"), 0, 100);
            final SAMRecordIterator samRecordIterator = ((SamReader.Indexing) reader).iterator(spanOverlapping);
            samRecordIterator.forEachRemaining(samRecord -> {
                annotations.add(new BamFeature(samRecord));
            });
            CloserUtil.close(reader);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public List<BamFeature> getAnnotations() {
        return annotations;
    }

}
