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
public interface ChromosomeSynomymService {

    void addSynonym(String preferredChromosomeName, String synonym);

    Optional<String> getPreferredChromosomeName(String synonym);

    void removeSynonym(String preferredChromosomeName, String synonym);

    boolean isSynonym(String preferredChromosomeName, String synonym);

}
