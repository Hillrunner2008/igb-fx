/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lorainelab.igb.persistencemanager;

import java.util.HashMap;
import java.util.Optional;

/**
 *
 * @author Devdatta Kulkarni
 */
public interface PersistenceManager {    
    void put(String key, String value);
    Optional<String> get(String key);
    boolean containsKey(String key);
    void remove(String key);
    void dropTable();
    void putAll(HashMap<String, String> valueMap);
    HashMap<String, String> getAll();
    void persistConnection();
    HashMap<String, String> getAllLike(String regex);
    //close connection
    //rollback
}
