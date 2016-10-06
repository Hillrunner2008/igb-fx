/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.impl;

import aQute.bnd.annotation.component.Component;
import java.io.IOException;
import org.lorainelab.igb.synonymservice.ChromosomeSynomymService;
import org.lorainelab.igb.synonymservice.GenomeVersionSynomymService;
import org.lorainelab.igb.synonymservice.util.CsvToJsonConverter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, provide = ChromosomeSynomymService.class)
public class ChromosomeSynomymServiceImpl extends SynonymServiceImpl implements ChromosomeSynomymService {

    //private SynonymServiceImpl service;
    private static final String CHROMOSOME_SYNONYM_FILE = "chromosomes.txt";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ChromosomeSynomymServiceImpl.class);

    public ChromosomeSynomymServiceImpl() {
        try {
            //service = new SynonymServiceImpl();
            String json = CsvToJsonConverter.loadCsvToJsonString(CHROMOSOME_SYNONYM_FILE);
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
    public String getChromosome(String synonym) {
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
