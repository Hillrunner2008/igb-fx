/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistencemanagerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.HashMap;
import java.util.Optional;
import org.lorainelab.igb.persistencemanager.PersistenceManager;
import org.osgi.service.jdbc.DataSourceFactory;
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
    private DataSourceFactory dataSourceFactory;
    
    public PersistencemanagerFactory() {
        persistenceManagers = new HashMap<String, PersistenceManager>();
    }

    public boolean isAvailable(String tableName) {
        return persistenceManagers.containsKey(tableName);
    }

    public synchronized Optional<PersistenceManager> getPersistenceManager(Class module) {
        String tableName = module.getName().replaceAll("\\.", "_");
        try {
            if (persistenceManagers.containsKey(tableName)) {
                return Optional.of(persistenceManagers.get(tableName));
            } else {
                PersistencemanagerImpl managerImpl = new PersistencemanagerImpl(tableName,dataSourceFactory);
                persistenceManagers.put(tableName, managerImpl);
                return Optional.of(managerImpl);
            }
        } catch (Exception e) {
            LOG.error("Failed to get persistance manager", e);
            return Optional.empty();
        }
    }

    @Activate
    public void Activate() {
        
    }
    
    @Reference(target = "(osgi.jdbc.driver.name=sqlite)")
    public void setDatasourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

}
