/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.impl;

import java.util.Optional;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lorainelab.igb.synonymservice.SpeciesSynomymService;

/**
 *
 * @author deva
 */
public class SpeciesSynomymServiceImplTest {

    private static SpeciesSynomymService service;

    public SpeciesSynomymServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        service = new SpeciesSynomymServiceImpl();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddSynonym() {
        String key = "test";
        String synonym = "testdata";
        String synonym1 = "testdata1";
        String synonym2 = "testdata2";

        service.addSynonym(key, synonym);
        service.addSynonym(key, synonym1);
        service.addSynonym(key, synonym2);

        assertTrue(service.isSynonym(key, synonym));
        assertTrue(service.getPreferredSpeciesName(synonym2).get().equals(key));
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

    @Test
    public void testIsSynonym() {
        String species = "Alligator mississippiensis";
        String synonym = "A_mississippiensis";
        SpeciesSynomymServiceImpl instance = new SpeciesSynomymServiceImpl();
        boolean expResult = true;
        boolean result = instance.isSynonym(species, synonym);
        assertEquals(expResult, result);

        synonym = "A_carolinensis";
        expResult = false;
        result = instance.isSynonym(species, synonym);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPreferredSpeciesName() {
        String synonym = "allMis";
        SpeciesSynomymServiceImpl instance = new SpeciesSynomymServiceImpl();
        Optional<String> expResult = Optional.of("Alligator mississippiensis");
        Optional<String> result = instance.getPreferredSpeciesName(synonym);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetCommonName() {
        String key = "test";
        String commonName = "testdata";
        SpeciesSynomymServiceImpl instance = new SpeciesSynomymServiceImpl();
        instance.setCommonName(key, commonName);
        Optional<String> expResult = Optional.of(commonName);
        Optional<String> result = instance.getCommonName(key);
        assertEquals(expResult, result);

        key = "test2";
        String syn = "testSyn";
        commonName = "testdata";
        instance.addSynonym(key, syn);
        instance.setCommonName(syn, commonName);
        expResult = Optional.of(commonName);
        result = instance.getCommonName(key);
        assertEquals(expResult, result);

        key = "test3";
        syn = "testSyn2";
        commonName = "testdata";
        instance.addSynonym(key, syn);
        instance.setCommonName(syn, commonName);
        expResult = Optional.of(commonName);
        result = instance.getCommonName("testSyn2");
        assertEquals(expResult, result);

    }

    @Test
    public void testIsCommonName() {
        String synonym = "allMis";
        String commonName = "American alligator";
        SpeciesSynomymServiceImpl instance = new SpeciesSynomymServiceImpl();
        boolean expResult = true;
        boolean result = instance.isCommonName(synonym, commonName);
        assertEquals(expResult, result);
    }

}
