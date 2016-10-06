/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice;

/**
 *
 * @author Devdatta Kulkarni
 */
public interface GenomeVersionSynomymService {

    void loadSynonymJson(String synonymJson);

    void addSynonym(String species, String synonym);

    String getGenomeVersion(String synonym);

    void removeSynonym(String genomeVersion, String synonym);

    boolean isSynonym(String genomeVersion, String synonym);

}
