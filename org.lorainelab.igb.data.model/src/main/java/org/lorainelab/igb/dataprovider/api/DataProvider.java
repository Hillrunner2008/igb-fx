package org.lorainelab.igb.dataprovider.api;

import com.google.common.collect.SetMultimap;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import org.lorainelab.igb.data.model.DataSet;

/**
 * TODO - add full description
 *
 * @author dcnorris
 */
public interface DataProvider {

    StringProperty name();

    StringProperty url();

    /**
     * It is expected this method will be called before any DataProvider content is loaded.
     * The expectation is implementors will wait for a call to this method before making remote request
     * to initialize content.
     */
    void initialize();

    Set<DataSet> getAvailableDataSets(String genomeVersionName);

    Set<String> getSupportedGenomeVersionNames();

    Optional<Map<String, Integer>> getAssemblyInfo(String genomeVersionName);

    default Optional<SetMultimap<String, String>> getGenomeVersionSynonyms() {
        return Optional.empty();
    }

    default Optional<String> getGenomeVersionDescription(String genomeVersionName) {
        return Optional.empty();
    }

    /**
     * @return Returns the default load priority
     * this priority will be used to determine the order
     * in which to query DataProvider instances. Users will be able to override the value returned here.
     */
    IntegerProperty loadPriority();

//    /**
//     * @return Optional mirror url for automatic failover
//     */
//    default Optional<String> getMirrorUrl() {
//        return Optional.empty();
//    }
//
//    void setMirrorUrl(String mirrorUrl);
//
//    boolean useMirrorUrl();
    /**
     * @return Checks and returns current server status,
     * this status drives
     */
    ResourceStatus getStatus();

    void setStatus(ResourceStatus status);

    Optional<String> getLogin();

    void setLogin(String login);

    Optional<String> getPassword();

    void setPassword(String password);

    Optional<URI> getSequenceFilePath(String name);

}
