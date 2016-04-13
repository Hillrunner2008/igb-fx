package org.lorainelab.igb.datasource.api;

/**
 *
 * @author dcnorris
 */
public class DataSourceReference {

    private final String path;
    private final DataSource dataSource;

    public DataSourceReference(String path, DataSource dataSource) {
        this.path = path;
        this.dataSource = dataSource;
    }

    public String getPath() {
        return path;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
