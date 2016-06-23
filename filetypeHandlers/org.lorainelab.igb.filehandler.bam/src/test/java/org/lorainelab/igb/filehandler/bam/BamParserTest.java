/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.filehandler.bam;

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
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

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
}
