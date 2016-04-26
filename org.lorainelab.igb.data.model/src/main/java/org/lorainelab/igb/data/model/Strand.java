/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model;

/**
 *
 * @author dcnorris
 */
public enum Strand {
    POSITIVE("+"), NEGATIVE("-");
    private final String name;

    private Strand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
