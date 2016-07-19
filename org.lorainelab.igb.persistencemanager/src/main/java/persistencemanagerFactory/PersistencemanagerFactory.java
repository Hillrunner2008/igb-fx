/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistencemanagerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import java.util.HashMap;
import java.util.Optional;
import lorainelab.igb.persistencemanager.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistencemanagerImpl.PersistencemanagerImpl;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, provide = PersistencemanagerFactory.class)
public class PersistencemanagerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PersistencemanagerFactory.class);
    private HashMap<String, PersistenceManager> persistenceManagers;

    public PersistencemanagerFactory() {
        persistenceManagers = new HashMap<String, PersistenceManager>();
    }

    public boolean isAvailable(String tableName) {
        return persistenceManagers.containsKey(tableName);
    }

    public synchronized Optional<PersistenceManager> getPersistenceManager(String tableName) {
        try {
            if (persistenceManagers.containsKey(tableName)) {
                return Optional.of(persistenceManagers.get(tableName));
            } else {
                PersistenceManager m = new PersistencemanagerImpl(tableName);
                persistenceManagers.put(tableName, m);
                return Optional.of(m);
            }
        }catch(Exception e){
            LOG.error("Failed to get persistance manager", e);
            return Optional.empty();
        }
    }

    @Activate
    public void Activate() {
    }

}
