package org.lorainelab.igb.data.model.util;

import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author dcnorris
 */
public class DataSourceUtils {

    private static ISeekableStreamFactory streamFactory = SeekableStreamFactory.getInstance();

    public static InputStream getStreamFor(String path) throws IOException {
        final SeekableStream stream = streamFactory.getStreamFor(path);
        if (path.endsWith("gz")) {
            try {
                if (BlockCompressedInputStream.isValidFile(stream)) {
                    return new BlockCompressedInputStream(stream);
                }
            } catch (Throwable ex) {
                return new GZIPInputStream(stream);
            }
        }
        return stream;
    }
}
