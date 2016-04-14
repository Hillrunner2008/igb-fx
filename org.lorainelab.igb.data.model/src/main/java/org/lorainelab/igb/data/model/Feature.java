/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model;

import com.google.common.collect.Range;
import java.util.Optional;

/**
 *
 * @author dcnorris
 */
public interface Feature {

    Range<Integer> getRange();

    Strand getStrand();

    Optional<String> getId();

    String getChromosomeId();

}
