package org.lorainelab.igb.filehandler.tabix;

import com.google.common.base.Strings;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.readers.AsciiLineReader;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.tribble.util.LittleEndianOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author dcnorris
 */
public class BedToTabixWriter {

    public static void writeIndex(File sortedBedFile, File sortedBedFileIndex) throws IOException {
        final BEDCodec bedCodec = new BEDCodec();
        Index index = IndexFactory.createLinearIndex(sortedBedFile, bedCodec);
        try (final LittleEndianOutputStream idxStream = new LittleEndianOutputStream(new BufferedOutputStream(new FileOutputStream(sortedBedFileIndex)));) {
            index.write(idxStream);
        }
    }

    public static void sortedBedToBGzip(File sortedBedFile, File outputBedFile) throws IOException {
        try (final SeekableStream is = SeekableStreamFactory.getInstance().getStreamFor(sortedBedFile.toURI().toURL());
                BlockCompressedOutputStream writer = new BlockCompressedOutputStream(outputBedFile);) {
            LineIterator lineIterator = new LineIteratorImpl(new AsciiLineReader(is));
            StringBuilder header = new StringBuilder();
            final String headerLineTest = lineIterator.next();
            String firstBedLine;
            if (isHeaderLine(headerLineTest)) {
                header.append(headerLineTest).append('\n');
                firstBedLine = lineIterator.peek();
            } else {
                firstBedLine = headerLineTest;
                writer.write(firstBedLine.getBytes());
                writer.write('\n');
            }
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                if (Strings.isNullOrEmpty(line)) {
                    continue;
                }
                writer.write(line.getBytes());
                writer.write('\n');
            }
        }
    }

    public static boolean isHeaderLine(final String line) {
        return line.startsWith("#") || line.startsWith("track") || line.startsWith("browser");
    }

}
