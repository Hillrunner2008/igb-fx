/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.search.api.model;

import com.google.common.collect.Maps;
import java.util.Map;

/**
 *
 * @author jeckstei
 */
public class Document {
    Map<String, String> fields;

    public Document() {
        this.fields = Maps.newHashMap();
    }

    public Map<String, String> getFields() {
        return fields;
    }

    
    
}
