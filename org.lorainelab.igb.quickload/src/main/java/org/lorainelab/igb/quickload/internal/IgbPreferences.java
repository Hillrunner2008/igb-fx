package org.lorainelab.igb.quickload.internal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dataProviders"
})
@XmlRootElement(name = "prefs")
public class IgbPreferences {

    @SerializedName("server")
    @Expose
    @XmlElement(name = "server")
    protected List<DataProviderConfig> dataProviders = new ArrayList<>();

    public List<DataProviderConfig> getDataProviders() {
        return dataProviders;
    }

    public void setDataProviders(List<DataProviderConfig> dataProviders) {
        this.dataProviders = dataProviders;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.dataProviders);
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
        final IgbPreferences other = (IgbPreferences) obj;
        if (!Objects.equals(this.dataProviders, other.dataProviders)) {
            return false;
        }
        return true;
    }

}
