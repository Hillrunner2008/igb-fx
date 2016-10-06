/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.impl;

import aQute.bnd.annotation.component.Component;
import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final String CHROMOSOME_SYNONYM_FILE = "chromosomes.json";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ChromosomeSynomymServiceImpl.class);

    public ChromosomeSynomymServiceImpl() {
        StringBuffer jsonData = new StringBuffer();
        try {
            InputStream resourceAsStream = CsvToJsonConverter.class.getClassLoader().getResourceAsStream(CHROMOSOME_SYNONYM_FILE);
            BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
            String line;
            while ((line = br.readLine()) != null) {
                jsonData.append(line.trim());
            }
        } catch (IOException ex) {
            Logger.getLogger(SpeciesSynomymServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!Strings.isNullOrEmpty(jsonData.toString())) {
            loadSynonymJson(jsonData.toString());
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
        removeSynonym(species, synonym);
    }

    @Override
    public boolean isSynonym(String species, String synonym) {
        return checkIfSynonym(species, synonym);
    }

    @Override
    public Optional<String> getPreferredChromosomeName(String synonym) {
        String name = getBaseWord(synonym);
        if (name != null) {
            return Optional.of(name);
        } else {
            return Optional.empty();
        }
    }

}
