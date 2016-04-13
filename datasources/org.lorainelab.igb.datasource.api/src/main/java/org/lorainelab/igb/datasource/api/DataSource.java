package org.lorainelab.igb.datasource.api;

import java.io.InputStream;
import java.util.Optional;

/**
 *
 * @author dcnorris
 */
public interface DataSource {

    DataSourceReference getDataSourceReference(String path);

    Optional<InputStream> getInputStream(String path);

    byte[] getByteRange(String path, int startPosition, int len);
}
