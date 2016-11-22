package org.lorainelab.igb.data.model.util;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import htsjdk.samtools.seekablestream.SeekableFTPStream;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.HttpUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.lorainelab.igb.cache.api.RemoteFileCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class DataSourceUtilsImpl implements DataSourceUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceUtilsImpl.class);
    private static RemoteFileCacheService cacheService;

    @Override
    public InputStream getStreamFor(final String path, boolean forceCache) throws IOException {
        URL url = null;
        if (path.startsWith("http") || path.startsWith("ftp:")) {
            url = new URL(path);
        } else if (path.startsWith("file:")) {
            return getStreamFor(new FileInputStream(new File(path)), path);
        } else {
            url = new File(path).toURI().toURL();
            return getStreamFor(new FileInputStream(new File(path)), path);
        }
        Optional<File> filebyUrl = cacheService.getFilebyUrl(url);
        if (filebyUrl.isPresent()) {
            return new FileInputStream(filebyUrl.get());
        } else {
            if (resourceAvailable(url)) {
                LOG.error("Error occurred while caching file, falling back to providing a stream to resource without caching");
                return getStreamFor(path);
            } else {
                throw new FileNotFoundException("Could not reach file (" + path + ")");
            }
        }
    }

    public static InputStream getStreamFor(final String path) throws IOException {
        if (cacheService != null) {
            URL url = null;
            if (path.startsWith("http") || path.startsWith("ftp:") || path.startsWith("file:")) {
                url = new URL(path);
            } else {
                url = new File(path).toURI().toURL();
            }
            if (cacheService.cacheExists(url)) {
                Optional<File> filebyUrl = cacheService.getFilebyUrl(url);
                if (filebyUrl.isPresent()) {
                    return new FileInputStream(filebyUrl.get());
                }
            }
        }
        if (path.startsWith("http")) {
            final URL url = new URL(path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            return getStreamFor(urlConnection.getInputStream(), path);
        } else if (path.startsWith("ftp:")) {
            return getStreamFor(new SeekableFTPStream(new URL(path)), path);
        } else if (path.startsWith("file")) {
            return getStreamFor(new FileInputStream(new File(new URL(path).getPath())), path);
        } else {
            return getStreamFor(new FileInputStream(new File(path)), path);
        }
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

    public static boolean resourceAvailable(final String path) {
        if (path.startsWith("http")) {
            try {
                return HttpUtils.resourceAvailable(new URL(path));
            } catch (MalformedURLException ex) {
                LOG.error(ex.getMessage(), ex);
                return false;
            }
        } else {
            return new File(path).exists();
        }
    }

    public static boolean resourceAvailable(final URL url) {
        String path = url.toExternalForm();
        if (path.startsWith("http")) {
            return HttpUtils.resourceAvailable(url);
        } else {
            return new File(path).exists();
        }
    }

    @Reference
    public void setCacheService(RemoteFileCacheService cacheService) {
        DataSourceUtilsImpl.cacheService = cacheService;
    }

}
