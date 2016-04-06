/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model;

import java.util.Optional;

/**
 *
 * @author dcnorris
 */
public interface Feature {

    Range getRange();

    Strand getStrand();

    Optional<String> getId();

    String getChromosomeId();

}
