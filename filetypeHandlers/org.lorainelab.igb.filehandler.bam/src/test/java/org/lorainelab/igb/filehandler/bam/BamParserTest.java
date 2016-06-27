/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.filehandler.bam;

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
import java.util.List;
import java.util.Optional;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class BamParserTest {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BamParserTest.class);

    public static void main(String [] args) {
        testChromosomeList();
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
        try (SeekableBufferedStream bamSeekableStream = new SeekableBufferedStream(
                    new SeekableFileStream(new File("/home/jeckstei/Downloads/cold_control.mm.bam")));
                    SeekableBufferedStream indexSeekableStream = new SeekableBufferedStream(
                            new SeekableFileStream(new File("/home/jeckstei/Downloads/cold_control.mm.bam.bai")));) {
                SamReader reader = SamReaderFactory.make()
                        .validationStringency(ValidationStringency.SILENT).open(SamInputResource.of(bamSeekableStream));
                final List<SAMSequenceRecord> seqRecords = reader.getFileHeader().getSequenceDictionary().getSequences();
                Optional<SAMSequenceRecord> sequence = getSequenceByName(chromosomeId, seqRecords);
                SAMSequenceRecord record = sequence.get();
                int start = record.getSequenceIndex();
                int end = start + record.getSequenceLength();
                
                try (SAMRecordIterator iter = reader.query(record.getSequenceName(), start, end, true)) {
                    while (iter.hasNext()) {
                        SAMRecord samRecord = iter.next();
                        annotations.add(new BamFeature(samRecord));
                    }
                }
                CloserUtil.close(reader);
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
    }
    
    private static Optional<SAMSequenceRecord> getSequenceByName(String sequence, List<SAMSequenceRecord> seqRecords) {
        return seqRecords.stream().filter(r -> sequence.equals(r.getSequenceName())).findFirst();
    }
}
