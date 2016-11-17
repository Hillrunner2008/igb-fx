package org.lorainelab.igb.quickload.internal;

import com.google.gson.annotations.Expose;
import java.util.Objects;

public class JsonWrapper {

    @Expose
    protected IgbPreferences prefs;

    public IgbPreferences getPrefs() {
        return prefs;
    }

    public void setPrefs(IgbPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.prefs);
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
        final JsonWrapper other = (JsonWrapper) obj;
        if (!Objects.equals(this.prefs, other.prefs)) {
            return false;
        }
        return true;
    }

}
