/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.impl;

import aQute.bnd.annotation.component.Component;
import java.io.IOException;
import org.lorainelab.igb.synonymservice.GenomeVersionSynomymService;
import org.lorainelab.igb.synonymservice.util.CsvToJsonConverter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, provide = GenomeVersionSynomymService.class)
public class GenomeVersionSynomymServiceImpl extends SynonymServiceImpl implements GenomeVersionSynomymService {

    private static final String SPECIES_SYNONYM_FILE = "synonyms.txt";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GenomeVersionSynomymServiceImpl.class);

    public GenomeVersionSynomymServiceImpl() {
        try {
            String json = CsvToJsonConverter.loadCsvToJsonString(SPECIES_SYNONYM_FILE);
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
    public String getGenomeVersion(String synonym) {
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
