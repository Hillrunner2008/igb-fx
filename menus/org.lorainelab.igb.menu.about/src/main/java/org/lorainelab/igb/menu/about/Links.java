/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.about;

/**
 *
 * @author dfreese
 */
public enum Links {
    AFFYMETRIX_LINK("Affymetrix", "http://www.affymetrix.com/estore/"),
    BIOVIZ_LINK("BioViz.org", "http://www.bioviz.org/igb/index.html"),
    CITATION_LINK ("Integrated genome browser: visual analytics platform for genomics.", 
            "http://bioinformatics.oxfordjournals.org/content/early/2016/04/04/bioinformatics.btw069");

    private final String value;
    private final String url;

    private Links(String value, String url) {
        this.value = value;
        this.url = url;
    }

    public String getValue() {
        return value;
    }

    public String url() {
        return url;
    }
}
