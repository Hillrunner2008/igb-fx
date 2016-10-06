/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.util.List;
import org.lorainelab.igb.synonymservice.SynonymService;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
public class SynonymServiceImpl implements SynonymService {

    public SynonymServiceImpl(){
        
    }
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SynonymServiceImpl.class);

    protected SetMultimap<String, String> thesaurus
            = Multimaps.synchronizedSetMultimap(LinkedHashMultimap.<String, String>create());
    
    protected SetMultimap<String, String> invertedMap
            = Multimaps.synchronizedSetMultimap(LinkedHashMultimap.<String, String>create());

    @Override
    public void storeSynonym(String key, String synonym) {
        if (!(thesaurus.get(key).contains(synonym))) {
            thesaurus.put(key, synonym);
            invertedMap.put(synonym, key);
        }
    }

    @Override
    public String getBaseWord(String synonym) {
        if (invertedMap.containsKey(synonym)) {
            return invertedMap.get(synonym).iterator().next();
        }
        return null;
    }

    @Override
    public void removeSynonym(String key, String synonym) {
        thesaurus.remove(key, synonym);
        invertedMap.remove(synonym, key);
    }

    @Override
    public boolean checkIfSynonym(String key, String synonym) {
        return thesaurus.containsEntry(key, synonym);
    }

    @Override
    public void loadSynonymJson(String synonymJson) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());

        final CollectionType javaType
                = mapper.getTypeFactory().constructCollectionType(List.class, LinkedHashMultimap.class);
        List<Multimap> asList = null;
        try {
            asList = mapper.readValue(
                    synonymJson, javaType);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        if (asList != null) {
            asList.forEach(l -> thesaurus.putAll(l));
            invertedMap = Multimaps.invertFrom(thesaurus, invertedMap);
        }
        LOG.trace("Loaded data from "+synonymJson);
    }
}
