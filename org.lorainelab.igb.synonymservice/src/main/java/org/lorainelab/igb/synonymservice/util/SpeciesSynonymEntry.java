/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.util;

import java.util.LinkedHashSet;

/**
 *
 * @author deva
 */
public class SpeciesSynonymEntry extends SynonymEntry {

    String commonName;

    public SpeciesSynonymEntry() {

    }

    public SpeciesSynonymEntry(String name) {
        this.preferredName = name;
        synomyms = new LinkedHashSet<>();
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

}
