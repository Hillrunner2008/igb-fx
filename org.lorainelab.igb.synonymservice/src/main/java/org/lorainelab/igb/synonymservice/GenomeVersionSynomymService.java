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
public interface GenomeVersionSynomymService {

    void addSynonym(String preferredGenomeVersionName, String synonym);

    Optional<String> getPreferredGenomeVersionName(String synonym);

    void removeSynonym(String preferredGenomeVersionName, String synonym);

    boolean isSynonym(String preferredGenomeVersionName, String synonym);

}
