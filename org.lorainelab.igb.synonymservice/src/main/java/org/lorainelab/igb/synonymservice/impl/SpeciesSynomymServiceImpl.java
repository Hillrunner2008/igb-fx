/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.impl;

import aQute.bnd.annotation.component.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.lorainelab.igb.synonymservice.SpeciesSynomymService;
import org.lorainelab.igb.synonymservice.util.SpeciesSynonymEntry;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true, provide = SpeciesSynomymService.class)
public class SpeciesSynomymServiceImpl extends SynonymServiceImpl implements SpeciesSynomymService {

    //private SynonymServiceImpl service;
    private static final String SPECIES_SYNONYM_FILE = "/species.json";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SpeciesSynomymServiceImpl.class);

    private Map<String, String> commonNameMap = new HashMap<>();

    public SpeciesSynomymServiceImpl() {
        String jsonData = null;
        try {
            jsonData = com.google.common.io.Resources.toString(SpeciesSynomymService.class.getResource(SPECIES_SYNONYM_FILE), Charsets.UTF_8);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        if (!Strings.isNullOrEmpty(jsonData)) {
            loadSynonymJson(jsonData);
        }
    }

    @Override
    public void loadSynonymJson(String synonymJson) {

        final ObjectMapper mapper = new ObjectMapper();
        final CollectionType javaType
                = mapper.getTypeFactory().constructCollectionType(List.class, SpeciesSynonymEntry.class);

        List<SpeciesSynonymEntry> asList = null;
        try {
            asList = mapper.readValue(
                    synonymJson, javaType);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }

        if (asList != null) {
            asList.forEach(l -> {
                thesaurus.putAll(l.getPreferredName(), l.getSynomyms());
                commonNameMap.put(l.getPreferredName(), l.getCommonName());
            });
        }
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
    public Optional<String> getPreferredSpeciesName(String synonym) {
        return getSpeciesNameFromGenomeVersionName(synonym);
    }

    @Override
    public Optional<String> getCommonName(String synonym) {
        String[] commonName = {null};
        getPreferredSpeciesName(synonym).ifPresent(prefName -> commonName[0] = commonNameMap.get(prefName));
        if (commonName[0] != null) {
            return Optional.of(commonName[0]);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void setCommonName(String synonym, String commonName) {
        getPreferredSpeciesName(synonym).ifPresent(prefName -> commonNameMap.put(prefName, commonName));
    }

    @Override
    public boolean isCommonName(String synonym, String commonName) {
        boolean[] common = {false};
        getPreferredSpeciesName(synonym).ifPresent(prefName -> common[0] = commonName.equals(commonNameMap.get(prefName)));
        return common[0];
    }

}
