/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.impl;

import java.io.IOException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.lorainelab.igb.synonymservice.GenomeVersionSynomymService;
import org.lorainelab.igb.synonymservice.SpeciesSynomymService;

/**
 *
 * @author Devdatta Kulkarni
 */
public class GenomeVersionSynonymServiceImplTest {

    //private static SpeciesSynomymService service;
    private static GenomeVersionSynomymService service;

    public GenomeVersionSynonymServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        service = new GenomeVersionSynomymServiceImpl();
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testStoreSynonym() {
        String key = "test";
        String synonym = "testdata";
        String synonym1 = "testdata1";
        String synonym2 = "testdata2";

        service.addSynonym(key, synonym);
        service.addSynonym(key, synonym1);
        service.addSynonym(key, synonym2);

        assertTrue(service.isSynonym(key, synonym));
        assertTrue(service.getPreferredGenomeVersionName(synonym2).get().equals(key));

        //
    }

    @Test
    public void testGetBaseWord() {
        //species synonym is already loaded in setup
        String expResult = "A_mississippiensis_Aug_2012";
        String synonym1 = "A_mississippiensis_Aug_2012";
        String synonym2 = "allMis1";
        assertEquals(expResult, service.getPreferredGenomeVersionName(synonym1).get());
        assertEquals(expResult, service.getPreferredGenomeVersionName(synonym2).get());
    }

    @Test
    public void testRemoveSynonym() {
        String key = "keyRemove";
        String synonym1 = "valueRemove1";
        String synonym2 = "valueRemove2";
        service.addSynonym(key, synonym1);
        service.addSynonym(key, synonym2);
        service.removeSynonym(key, synonym2);
        assertFalse(service.isSynonym(key, synonym2));
    }

//    @Test
//    public void testLoadSynonymJson() {
//        String jsonString = "[{\"DemoKeyJson\":[\"demovalue1\",\"demovalue2\",\"demovalue4\",\"demovalue3\",\"demovalue3\"]}]";
//        service.loadSynonymJson(jsonString);
//        assertEquals("DemoKeyJson", service.getBaseWord("demovalue1"));
//        assertTrue(service.checkIfSynonym("DemoKeyJson", "demovalue4"));
//
//    }

}
