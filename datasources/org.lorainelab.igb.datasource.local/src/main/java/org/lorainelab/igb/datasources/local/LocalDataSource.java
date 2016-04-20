package org.lorainelab.igb.datasources.local;

import aQute.bnd.annotation.component.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.lorainelab.igb.data.model.datasource.DataSource;
import org.lorainelab.igb.data.model.datasource.DataSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, name = LocalDataSource.COMPONENT_NAME)
public class LocalDataSource implements DataSource {

    public static final String COMPONENT_NAME = "LocalDataSource";
    private static final Logger LOG = LoggerFactory.getLogger(LocalDataSource.class);


    @Override
    public Optional<InputStream> getInputStream(String path) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(path));
        } catch (FileNotFoundException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return Optional.ofNullable(fileInputStream);
    }

    @Override
    public byte[] getByteRange(String path, int startPosition, int len) {
        byte[] bytes = new byte[len];
        getInputStream(path).ifPresent(stream -> {
            try {
                ((FileInputStream) stream).read(bytes, startPosition, len);
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        });
        return bytes;
    }

    @Override
    public DataSourceReference getDataSourceReference(String path) {
        return new DataSourceReference(path, this);
    }

}
