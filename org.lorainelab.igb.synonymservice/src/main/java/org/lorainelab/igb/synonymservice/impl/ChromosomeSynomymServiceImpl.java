/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.impl;

import aQute.bnd.annotation.component.Component;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Optional;
import org.lorainelab.igb.synonymservice.ChromosomeSynomymService;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, provide = ChromosomeSynomymService.class)
public class ChromosomeSynomymServiceImpl extends SynonymServiceImpl implements ChromosomeSynomymService {

    //private SynonymServiceImpl service;
    private static final String CHROMOSOME_SYNONYM_FILE = "/chromosomes.json";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ChromosomeSynomymServiceImpl.class);

    public ChromosomeSynomymServiceImpl() {
        String jsonData = null;
         try {
            jsonData = com.google.common.io.Resources.toString(ChromosomeSynomymService.class.getResource(CHROMOSOME_SYNONYM_FILE), Charsets.UTF_8);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        if (!Strings.isNullOrEmpty(jsonData)) {
            loadSynonymJson(jsonData);
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
    public void removeSynonym(String species, String synonym) {
        super.removeSynonym(species, synonym);
    }

    @Override
    public boolean isSynonym(String species, String synonym) {
        return checkIfSynonym(species, synonym);
    }

    @Override
    public Optional<String> getPreferredChromosomeName(String synonym) {
        return getBaseWord(synonym);
    }

}
