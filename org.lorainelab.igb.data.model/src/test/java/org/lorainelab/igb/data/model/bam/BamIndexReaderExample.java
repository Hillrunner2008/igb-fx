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
import java.io.FileNotFoundException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class BamIndexReaderExample {

    private static final Logger LOG = LoggerFactory.getLogger(BamIndexReaderExample.class);

    @Test
    public void queryBamFileFromIndex() throws FileNotFoundException {
        SeekableBufferedStream bamSeekableStream = new SeekableBufferedStream(new SeekableFileStream(new File(BamIndexReaderExample.class.getClassLoader().getResource("small.bam").getFile())));
        SeekableBufferedStream indexSeekableStream = new SeekableBufferedStream(new SeekableFileStream(new File(BamIndexReaderExample.class.getClassLoader().getResource("small.bam.bai").getFile())));
        final SAMSequenceDictionary samSequenceDictionary = new SAMSequenceDictionary();
        samSequenceDictionary.addSequence(new SAMSequenceRecord("chr1", 30427671));
        final DiskBasedBAMFileIndex bamIndex = new DiskBasedBAMFileIndex(indexSeekableStream, samSequenceDictionary);
        SamReader reader = SamReaderFactory.make().validationStringency(ValidationStringency.SILENT).open(SamInputResource.of(bamSeekableStream));
        BAMFileSpan spanOverlapping = bamIndex.getSpanOverlapping(samSequenceDictionary.getSequenceIndex("chr1"), 0, 100);
        final SAMRecordIterator samRecordIterator = ((SamReader.Indexing) reader).iterator(spanOverlapping);

        samRecordIterator.forEachRemaining(samRecord
                -> {
            LOG.info(samRecord.getCigarString());
        }
        );
        CloserUtil.close(reader);
    }
}
