package org.lorainelab.igb.data.model.util;

import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 *
 * @author dcnorris
 */
public class DataSourceUtils {

    private static ISeekableStreamFactory STREAM_FACTORY = SeekableStreamFactory.getInstance();

    public static InputStream getStreamFor(String path) throws IOException {
        final SeekableStream stream = STREAM_FACTORY.getStreamFor(path);
        return getStreamFor(stream, path);
    }

    public static InputStream getStreamFor(InputStream istr, String path) throws IOException {
        String lc_stream_name = path.toLowerCase();
        if (lc_stream_name.endsWith(".gz") || lc_stream_name.endsWith(".gzip")
                || lc_stream_name.endsWith(".z")) {
            InputStream gzstr = getGZipInputStream(path, istr);
            String updatedPath = path.substring(0, path.lastIndexOf('.'));
            return getStreamFor(gzstr, updatedPath);
        } else if (lc_stream_name.endsWith(".zip")) {
            ZipInputStream zstr = new ZipInputStream(istr);
            zstr.getNextEntry();
            String updatedPath = path.substring(0, path.lastIndexOf('.'));
            return getStreamFor(zstr, updatedPath);
        } else if (lc_stream_name.endsWith(".bz2")) {
            BZip2CompressorInputStream bz2 = new BZip2CompressorInputStream(istr);
            String updatedPath = path.substring(0, path.lastIndexOf('.'));
            return getStreamFor(bz2, updatedPath);
        } else if (lc_stream_name.endsWith(".tar")) {
            TarArchiveInputStream tarInput = new TarArchiveInputStream(istr);
            tarInput.getNextTarEntry();
            String updatedPath = path.substring(0, path.lastIndexOf('.'));
            return getStreamFor(tarInput, updatedPath);
        }
        return istr;
    }

    public static InputStream getGZipInputStream(String path, InputStream stream) throws IOException {
        try {
            if (BlockCompressedInputStream.isValidFile(stream)) {
                return new BlockCompressedInputStream(stream);
            }
            return new GZIPInputStream(stream);
        } catch (Throwable ex) {
            return new GZIPInputStream(stream);
        }
    }

}
