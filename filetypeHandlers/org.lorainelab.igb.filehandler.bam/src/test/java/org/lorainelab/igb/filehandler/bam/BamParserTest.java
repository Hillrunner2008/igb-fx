/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.filehandler.bam;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.QueryInterval;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
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
import java.util.Optional;
import java.util.Set;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 *
 * @author jeckstei
 */
public class BamParserTest {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BamParserTest.class);

    public static void main(String[] args) {
        testGetRecordsBySequence("chr1");
        Range<Integer> range = Range.closed(30_000, 32_000);
        testGetRecordsByRangeInSequence("chr1", range);
    }

    
    

   // @Test
    public static void testChromosomeList() {
        try (SeekableBufferedStream bamSeekableStream = new SeekableBufferedStream(
                new SeekableFileStream(new File("/home/jeckstei/Downloads/cold_control.mm.bam")));
                SeekableBufferedStream indexSeekableStream = new SeekableBufferedStream(
                        new SeekableFileStream(new File("/home/jeckstei/Downloads/cold_control.mm.bam.bai")));) {
            SamReader reader = SamReaderFactory.make()
                    .validationStringency(ValidationStringency.SILENT).open(SamInputResource.of(bamSeekableStream));

            LOG.info("getting seq");
            final List<SAMSequenceRecord> seqRecords = reader.getFileHeader().getSequenceDictionary().getSequences();
            for (final SAMSequenceRecord seqRecord : seqRecords) {
                if (seqRecord.getSequenceName() != null) {
                    LOG.info(seqRecord.getSequenceName());
                }
            }

            CloserUtil.close(reader);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public static void testGetRecordsBySequence(String chromosomeId) {
        List<BamFeature> annotations = new ArrayList<>();
        try (SeekableBufferedStream bamSeekableStream = new SeekableBufferedStream(
                new SeekableFileStream(new File("/home/jeckstei/Downloads/cold_control.mm.bam")));
                SeekableBufferedStream indexSeekableStream = new SeekableBufferedStream(
                        new SeekableFileStream(new File("/home/jeckstei/Downloads/cold_control.mm.bam.bai")));) {
            SamReader reader = SamReaderFactory.make()
                    .validationStringency(ValidationStringency.SILENT)
                    .open(SamInputResource.of(bamSeekableStream).index(indexSeekableStream));

            final List<SAMSequenceRecord> seqRecords = reader.getFileHeader().getSequenceDictionary().getSequences();
            Optional<SAMSequenceRecord> sequence = getSequenceByName(chromosomeId, seqRecords);
            if (sequence.isPresent()) {
                SAMSequenceRecord record = sequence.get();
                int start = record.getSequenceIndex();
                int end = start + record.getSequenceLength();

                try (SAMRecordIterator iter = reader.query(record.getSequenceName(), start, end, true)) {
                    while (iter.hasNext()) {
                        SAMRecord samRecord = iter.next();
                        annotations.add(new BamFeature(samRecord));
                    }
                }
            }
            CloserUtil.close(reader);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        LOG.info("features in " + chromosomeId + ": {}", annotations.size());
        
        Assert.assertTrue(annotations.size() > 0);
        for(int i = 0; i < 10; i++) {
            BamFeature feature = annotations.get(i);
            LOG.info("start {} end {}", feature.getRange().lowerEndpoint(), feature.getRange().upperEndpoint());
        }
    }

    public static void testGetRecordsByRangeInSequence(String chromosomeId, Range<Integer> range) {
        List<BamFeature> annotations = new ArrayList<>();
        try (SeekableBufferedStream bamSeekableStream = new SeekableBufferedStream(
                new SeekableFileStream(new File("/home/jeckstei/Downloads/cold_control.mm.bam")));
                SeekableBufferedStream indexSeekableStream = new SeekableBufferedStream(
                        new SeekableFileStream(new File("/home/jeckstei/Downloads/cold_control.mm.bam.bai")));) {
            SamReader reader = SamReaderFactory.make()
                    .validationStringency(ValidationStringency.SILENT)
                    .open(SamInputResource.of(bamSeekableStream).index(indexSeekableStream));

            final List<SAMSequenceRecord> seqRecords = reader.getFileHeader().getSequenceDictionary().getSequences();

            Optional<SAMSequenceRecord> sequence = getSequenceByName(chromosomeId, seqRecords);
            if (sequence.isPresent()) {
                SAMSequenceRecord record = sequence.get();
                QueryInterval[] intervals = new QueryInterval[]{
                    new QueryInterval(record.getSequenceIndex(), range.lowerEndpoint(), range.upperEndpoint())
                };

                try (SAMRecordIterator iter = reader.query(intervals, true)) {
                    while (iter.hasNext()) {
                        SAMRecord samRecord = iter.next();
                        annotations.add(new BamFeature(samRecord));
                    }
                }
            }
            CloserUtil.close(reader);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        LOG.info("features in " + chromosomeId + ": {}", annotations.size());
        Assert.assertTrue(annotations.size() > 0);
    }

    private static Optional<SAMSequenceRecord> getSequenceByName(String sequence, List<SAMSequenceRecord> seqRecords) {
        return seqRecords.stream().filter(r -> sequence.equals(r.getSequenceName())).findFirst();
    }
}
