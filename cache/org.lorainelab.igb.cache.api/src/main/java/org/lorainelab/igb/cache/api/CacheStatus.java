package org.lorainelab.igb.cache.api;

import java.io.File;
import java.math.BigInteger;

public class CacheStatus {

    private String md5;
    private long lastModified;
    private long cacheLastUpdate;
    private String etag;
    private boolean dataAvailable;
    private String url;
    private File data;
    private BigInteger size;
    private boolean isCorrupt = false;

    public BigInteger getSize() {
        return size;
    }

    public void setSize(BigInteger size) {
        this.size = size;
    }

    public long getCacheLastUpdate() {
        return cacheLastUpdate;
    }

    public void setCacheLastUpdate(long cacheLastUpdate) {
        this.cacheLastUpdate = cacheLastUpdate;
    }

    public File getData() {
        return data;
    }

    public void setData(File data) {
        this.data = data;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public boolean isDataAvailable() {
        return dataAvailable;
    }

    public void setDataAvailable(boolean dataExists) {
        this.dataAvailable = dataExists;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isCorrupt() {
        return isCorrupt;
    }

    public void setIsCorrupt(boolean isCorrupt) {
        this.isCorrupt = isCorrupt;
    }
    
}
