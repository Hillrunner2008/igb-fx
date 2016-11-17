package org.lorainelab.igb.quickload.internal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
public class DataProviderConfig {

    @XmlValue
    protected String value;
    @Expose
    @XmlAttribute(name = "name")
    protected String name;
    @Expose
    @XmlAttribute(name = "url")
    protected String url;
    @Expose
    @XmlAttribute(name = "loadPriority")
    protected Integer loadPriority;
    @SerializedName("default")
    @Expose
    @XmlAttribute(name = "default")
    protected String _default;
    @Expose
    @XmlAttribute(name = "mirror")
    protected String mirror;
    @Expose
    @XmlAttribute(name = "isEditable")
    protected boolean editable;
    @Expose
    @XmlAttribute(name = "status")
    protected String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String value) {
        this.url = value;
    }

    public Integer getLoadPriority() {
        return loadPriority;
    }

    public void setLoadPriority(Integer value) {
        this.loadPriority = value;
    }

    public String getDefault() {
        return _default;
    }

    public void setDefault(String value) {
        this._default = value;
    }

    public String getMirror() {
        return mirror;
    }

    public void setMirror(String value) {
        this.mirror = value;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.value);
        hash = 61 * hash + Objects.hashCode(this.name);
        hash = 61 * hash + Objects.hashCode(this.url);
        hash = 61 * hash + Objects.hashCode(this.loadPriority);
        hash = 61 * hash + Objects.hashCode(this.mirror);
        hash = 61 * hash + (this.editable ? 1 : 0);
        hash = 61 * hash + Objects.hashCode(this.status);
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
        final DataProviderConfig other = (DataProviderConfig) obj;
        if (this.editable != other.editable) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        if (!Objects.equals(this.mirror, other.mirror)) {
            return false;
        }
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.loadPriority, other.loadPriority)) {
            return false;
        }
        return true;
    }

}
