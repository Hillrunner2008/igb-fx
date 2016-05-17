package org.lorainelab.igb.data.model.datasource;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataSourceReference other = (DataSourceReference) obj;
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        return true;
    }

}
