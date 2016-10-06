/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.impl;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import java.io.IOException;
import org.lorainelab.igb.synonymservice.SpeciesSynomymService;
import org.lorainelab.igb.synonymservice.util.CsvToJsonConverter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, provide = SpeciesSynomymService.class)
public class SpeciesSynomymServiceImpl extends SynonymServiceImpl implements SpeciesSynomymService {

    //private SynonymServiceImpl service;
    private static final String SPECIES_SYNONYM_FILE = "species.txt";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SpeciesSynomymServiceImpl.class);

    public SpeciesSynomymServiceImpl() {
        try {
            //service = new SynonymServiceImpl();
            String json = CsvToJsonConverter.loadCsvToJsonString(SPECIES_SYNONYM_FILE);
            //service.loadSynonymJson(json);
            loadSynonymJson(json);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
    

    @Override
    public void loadSynonymJson(String synonymJson) {
        super.loadSynonymJson(synonymJson);
    }

    @Override
    public void addSynonym(String species, String synonym) {
        storeSynonym(species, synonym);
    }

    @Override
    public String getSpecies(String synonym) {
        return getBaseWord(synonym);
    }

    @Override
    public void removeSynonym(String species, String synonym) {
        removeSynonym(species, synonym);
    }

    @Override
    public boolean isSynonym(String species, String synonym) {
        return checkIfSynonym(species, synonym);
    }

}
