/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.lorainelab.igb.synonymservice.SynonymService;
import org.lorainelab.igb.synonymservice.util.SynonymEntry;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
public class SynonymServiceImpl implements SynonymService {

    private static final Pattern STANDARD_REGEX = Pattern.compile("^([a-zA-Z]+_[a-zA-Z]+).*$");
    private static final Pattern UCSC_REGEX = Pattern.compile("^([a-zA-Z]{2,6})[\\d]+$");
    private static final Pattern NON_STANDARD_REGEX = Pattern.compile("^([a-zA-Z]+_[a-zA-Z]+_[a-zA-Z]+).*$");

    public SynonymServiceImpl() {

    }

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SynonymServiceImpl.class);

    protected SetMultimap<String, String> thesaurus
            = Multimaps.synchronizedSetMultimap(LinkedHashMultimap.<String, String>create());

    @Override
    public void storeSynonym(String key, String synonym) {
        if (!(thesaurus.get(key).contains(synonym))) {
            thesaurus.put(key, synonym);
        }
    }

    @Override
    public Optional<String> getBaseWord(String synonym) {
        return Optional.of(thesaurus.entries().stream()
                .filter((Map.Entry<String, String> entry) -> entry.getValue().equalsIgnoreCase(synonym))
                .map(entry -> entry.getKey())
                .findFirst()
                .orElse(synonym));
    }

    public Optional<String> getSpeciesNameFromGenomeVersionName(String version) {
        String species = getSpeciesName(version, STANDARD_REGEX).orElse(version);
        if (species.equals(version)) {
            species = getSpeciesName(version, NON_STANDARD_REGEX).orElse(version);
        }
        if (species.equals(version)) {
            species = getSpeciesName(version, UCSC_REGEX).orElse(version);
        }
        Pattern pattern = Pattern.compile("(\\S+)(?>_(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)_\\d{4})");
        Matcher m = pattern.matcher(species);
        if (m.find()) {
            species = m.group(1);
        } else {
            pattern = Pattern.compile("^([a-zA-Z]{2,6})[\\d]+$");
            m = pattern.matcher(species);
            if (m.find()) {
                species = species.replaceAll("[\\d]+", "");
            }
        }

        pattern = Pattern.compile("([a-zA-Z]+)((_[a-zA-Z]+).*)");
        m = pattern.matcher(species);
        if (m.find()) {
            species = m.group(1) + m.group(2);
        }
        final String decodedSpecies = species;
        return Optional.of(thesaurus.entries().stream()
                .filter((Map.Entry<String, String> entry) -> entry.getValue().equalsIgnoreCase(decodedSpecies))
                .map(entry -> entry.getKey())
                .findFirst()
                .orElse(species));
    }

    private Optional<String> getSpeciesName(String version, Pattern regex) {
        Matcher matcher = regex.matcher(version);
        String matched = null;
        if (matcher.matches()) {
            matched = matcher.group(1);
        }
        if (matched == null || matched.isEmpty()) {
            return Optional.empty();
        }
        return getBaseWord(matched);

    }

    @Override
    public void removeSynonym(String key, String synonym) {
        thesaurus.remove(key, synonym);
    }

    @Override
    public boolean checkIfSynonym(String key, String synonym) {
        return thesaurus.containsEntry(key, synonym);
    }

    @Override
    public void loadSynonymJson(String synonymJson) {

        final ObjectMapper mapper = new ObjectMapper();
        final CollectionType javaType
                = mapper.getTypeFactory().constructCollectionType(List.class, SynonymEntry.class);

        List<SynonymEntry> asList = null;
        try {
            asList = mapper.readValue(
                    synonymJson, javaType);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }

        if (asList != null) {
            asList.forEach(l -> thesaurus.putAll(l.getPreferredName(), l.getSynomyms()));
        }
    }

    @Override
    public Set<String> getSynonyms(String synonym) {
        return thesaurus.get(synonym);
    }
}
