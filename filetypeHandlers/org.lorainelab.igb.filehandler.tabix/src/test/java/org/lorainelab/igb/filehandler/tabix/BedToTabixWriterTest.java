/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.filehandler.tabix;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.index.linear.LinearIndex;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.readers.LineIteratorImpl;
import static htsjdk.tribble.util.TabixUtils.STANDARD_INDEX_EXTENSION;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import static org.lorainelab.igb.filehandler.tabix.BedToTabixWriter.isHeaderLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class BedToTabixWriterTest {

    private static final Logger LOG = LoggerFactory.getLogger(BedToTabixWriterTest.class);

    @Test
    public void writeIndexTest() throws IOException {
        File sortedBedFile = new File("src/test/resources/sortedSample.bed");
        File sortedBedFileIndex = new File("target/sortedSample.bed" + STANDARD_INDEX_EXTENSION);
        sortedBedFileIndex.deleteOnExit();
        BedToTabixWriter.writeIndex(sortedBedFile, sortedBedFileIndex);
        final LinearIndex index2 = new LinearIndex(SeekableStreamFactory.getInstance().getStreamFor(sortedBedFileIndex.toURI().toURL()));
        Assert.assertTrue(index2.containsChromosome("chr2"));
    }

    @Test
    public void sortedBedToBGzipTest() throws IOException {
        File sortedBedFile = new File("src/test/resources/sortedSample.bed");
        File outputBedFile = new File("target/sortedSample.bed.gz");
        BedToTabixWriter.sortedBedToBGzip(sortedBedFile, outputBedFile);
        BufferedReader reader = new BufferedReader(new FileReader(sortedBedFile));
        String firstBedLine = reader.readLine();
        reader.close();
        try (final BlockCompressedInputStream bcis = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getStreamFor(outputBedFile.toURI().toURL()));) {
            LineIterator compressedFileIterator = new LineIteratorImpl(new AsciiLineReader(bcis));
            String firstCompressedBedLine = compressedFileIterator.next();
            if (isHeaderLine(firstCompressedBedLine)) {
                final String next = compressedFileIterator.next();
                Assert.assertTrue(firstBedLine.equals(next));
            } else {
                Assert.assertTrue(firstBedLine.equals(firstCompressedBedLine));
            }
        }
    }

}
