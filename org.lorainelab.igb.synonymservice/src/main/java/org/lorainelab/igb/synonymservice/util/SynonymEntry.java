/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.util;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Devdatta Kulkarni
 */
public class SynonymEntry {

    String preferredName;
    Set<String> synomyms;

    public SynonymEntry(){
        
    }
    
    public SynonymEntry(String name, Set<String> synomyms) {
        this.preferredName = name;
        this.synomyms = synomyms;
    }

    public SynonymEntry(String name) {
        this.preferredName = name;
        synomyms = new LinkedHashSet<>();
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public Set<String> getSynomyms() {
        return synomyms;
    }

    public void setSynomyms(Set<String> synomyms) {
        this.synomyms = synomyms;
    }

    public void addSynomym(String synonym) {
        this.synomyms.add(synonym);

    }

}
