/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice;

import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Devdatta Kulkarni
 */
public interface SynonymService {

    void loadSynonymJson(String synonymJson);

    void storeSynonym(String key, String synonym);

    Optional<String> getBaseWord(String synonym);

    Set<String> getSynonyms(String synonym);

    void removeSynonym(String key, String synonym);

    boolean checkIfSynonym(String key, String synonym);

}
