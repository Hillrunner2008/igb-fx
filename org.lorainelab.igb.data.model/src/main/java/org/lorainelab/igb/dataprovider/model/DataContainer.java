package org.lorainelab.igb.dataprovider.model;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.Sets;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.dataprovider.api.DataProvider;
import org.lorainelab.igb.dataprovider.api.StringVersionDateComparator;

public final class DataContainer implements Comparable<DataContainer> {

    private final GenomeVersion genomeVersion;
    private final DataProvider dataProvider;
    private final ObservableSet<DataSet> dataSets;
    private BooleanProperty isInitialized;

    public DataContainer(GenomeVersion genomeVersion, DataProvider dataProvider) {
        this.isInitialized = new SimpleBooleanProperty(false);
        this.genomeVersion = checkNotNull(genomeVersion);
        this.dataProvider = dataProvider;
        dataSets = FXCollections.observableSet(Sets.newConcurrentHashSet());
    }

    public BooleanProperty isInitialized() {
        return isInitialized;
    }

    public ObservableSet<DataSet> dataSets() {
        return dataSets;
    }

    @Override
    public String toString() {
        return genomeVersion.name().get();
    }

    @Override
    public int compareTo(DataContainer other) {
        return new StringVersionDateComparator().compare(this.getName(), other.getName());
    }

    /**
     * @return the genomeVersion
     */
    public GenomeVersion getGenomeVersion() {
        return genomeVersion;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.genomeVersion.name().get();
    }

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.genomeVersion);
        hash = 47 * hash + Objects.hashCode(this.dataProvider);
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
        final DataContainer other = (DataContainer) obj;
        if (!Objects.equals(this.genomeVersion, other.genomeVersion)) {
            return false;
        }
        if (!Objects.equals(this.dataProvider, other.dataProvider)) {
            return false;
        }
        return true;
    }

}
