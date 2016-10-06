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
public class CsvData {

    String name;
    Set<String> synomyms;

    public CsvData(String name, Set<String> synomyms) {
        this.name = name;
        this.synomyms = synomyms;
    }

    public CsvData(String name) {
        this.name = name;
        synomyms = new LinkedHashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
