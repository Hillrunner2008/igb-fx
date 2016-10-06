/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.util;

import org.junit.Test;
import org.lorainelab.igb.synonymservice.impl.SynonymServiceImpl;

/**
 *
 * @author deva
 */
public class CsvToJsonConverterTest {

    private static final String SPECIES_SYNONYM_FILE = "species.txt";
    String st = "[{\"Ailuropoda melanoleuca\":[\"Ailuropoda melanoleuca\",\"Giant panda\",\"A_melanoleuca\",\"ailMel\",\"Ailuropoda_melanoleuca\"]}]";
    public CsvToJsonConverterTest() {
    }

    @Test
    public void testLoadSynonyms() throws Exception {

        String json = CsvToJsonConverter.loadCsvToJsonString(SPECIES_SYNONYM_FILE);
        SynonymServiceImpl impl = new SynonymServiceImpl();
        impl.loadSynonymJson(json);
    }

}
