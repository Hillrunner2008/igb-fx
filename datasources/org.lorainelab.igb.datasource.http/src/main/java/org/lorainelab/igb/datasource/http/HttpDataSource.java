/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.datasource.http;

import aQute.bnd.annotation.component.Component;
import com.github.kevinsawicki.http.HttpRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.lorainelab.igb.data.model.datasource.DataSource;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, name = HttpDataSource.COMPONENT_NAME)
public class HttpDataSource implements DataSource {

    public static final String COMPONENT_NAME = "HttpDataSource";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HttpDataSource.class);

    @Override
    public DataSourceReference getDataSourceReference(String path) {
        return new DataSourceReference(path, this);
    }

    public static String toExternalForm(String urlString) {
        urlString = urlString.trim();
        if (!urlString.endsWith("/")) {
            urlString += "/";
        }
        return urlString;
    }

    @Override
    public Optional<InputStream> getInputStream(String path) {

        HttpRequest remoteHttpRequest = HttpRequest.get(path)//toExternalForm(path), true)
                .acceptGzipEncoding()
                .uncompress(true)
                .trustAllCerts()
                .trustAllHosts()
                .followRedirects(true);
        LOG.info(remoteHttpRequest.toString());
        InputStream inputStream = remoteHttpRequest.buffer();
        return Optional.of(inputStream);
    }

    @Override
    public byte[] getByteRange(String path, int startPosition, int len) {
        HttpRequest remoteHttpRequest = HttpRequest.get(toExternalForm(path), true)
                .acceptGzipEncoding()
                .uncompress(true)
                .trustAllCerts()
                .trustAllHosts()
                .followRedirects(true);
        LOG.info(remoteHttpRequest.toString());
        InputStream inputStream = remoteHttpRequest.buffer();
        byte[] data = new byte[len];
        try {
            int read = inputStream.read(data, startPosition, len);
            return data;
        } catch (IOException ex) {
            LOG.error("failed to read data from remote " + ex.getMessage(), ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }
        }
        return null;
    }
}
