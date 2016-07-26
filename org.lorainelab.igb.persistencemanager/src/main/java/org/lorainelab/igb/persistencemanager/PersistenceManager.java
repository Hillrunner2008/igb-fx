/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.persistencemanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This interface specifies methods that will be provided by implementation of Persistent management system.
 * The Persistence Management system should be created using @code PersistencemanagerFactory.
 * 
 * @author Devdatta Kulkarni
 */
public interface PersistenceManager {  
    /**
     * Persists the key and associated value. If there is previous value stored for the key
     * it will be replaced by new value.
     * @param key - key with which the specified value is to be associated.
     * @param value - value to be associated with key.
     */
    void put(String key, String value);
    /**
     * Retrieves the value associate with given key.
     * @param key - the key whose associated value is to be returned
     * @return Optional containing value associated with key or empty if key does not exist. 
     */
    Optional<String> get(String key);
    /**
     * Checks whether a key and associated value is present or not.
     * @param key - key whose presence is to be checked
     * @return true if specified key is available
     */
    boolean containsKey(String key);
    /**
     * Removes the key and associated value.
     * @param key -  key whose mapping is to be removed
     */
    void remove(String key);
    /**
     * Removes all keys and associated values.
     */
    void clear();
    /**
     * Bulk add method to put all values in the persistence management system.
     * @param valueMap - Map<String,String> containing keys and associated values.
     */
    void putAll(Map<String, String> valueMap);
    /**
     * Bulk get method to retrieve all keys and associated values.
     * @return the <String, String> map containing all keys and values.
     */
    Map<String, String> getAll();
    /**
     * Bulk get method to retrieve all keys that have a text pattern in them. 
     * Works similar to like clause in SQL
     * @param pattern - which is to present in key.
     * @return the <String, String> map containing all keys containing pattern and associated values.
     */
    Map<String, String> getAllLike(String pattern);
    //void persistConnection();
    //close connection
    //rollback
}
