/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice;

import java.util.Optional;

/**
 *
 * @author Devdatta Kulkarni
 */
public interface SpeciesSynomymService {

    void addSynonym(String preferredSpeciesName, String synonym);

    Optional<String> getPreferredSpeciesName(String synonym);

    Optional<String> getCommonName(String synonym);

    void setCommonName(String synonym, String commonName);

    void removeSynonym(String preferredSpeciesName, String synonym);

    boolean isSynonym(String preferredSpeciesName, String synonym);

    boolean isCommonName(String synonym, String commonName);

}
