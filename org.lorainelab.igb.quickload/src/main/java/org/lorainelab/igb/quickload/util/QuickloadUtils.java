package org.lorainelab.igb.quickload.util;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.lorainelab.igb.data.model.util.DataSourceUtils;
import org.lorainelab.igb.dataprovider.api.SpeciesInfo;
import org.lorainelab.igb.quickload.QuickloadConstants;
import static org.lorainelab.igb.quickload.QuickloadConstants.ANNOTS_XML;
import org.lorainelab.igb.quickload.model.QuickloadFile;
import org.lorainelab.igb.synonymservice.GenomeVersionSynomymService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = QuickloadUtils.class)
public class QuickloadUtils {

    private static final Logger LOG = LoggerFactory.getLogger(QuickloadUtils.class);
    private static final AnnotsParser ANNOTS_PARSER = new AnnotsParser();
    private DataSourceUtils dataSourceUtils;
    private GenomeVersionSynomymService genomeVersionSynomymService;

    public void loadGenomeVersionSynonyms(String urlString, Multimap<String, String> genomeVersionSynonyms) {
        try {
            urlString = toExternalForm(urlString);
            urlString += QuickloadConstants.SYNONYMS_TXT;
            parseGenomeVersionSynonyms(dataSourceUtils.getStreamFor(urlString, true), genomeVersionSynonyms);
        } catch (Exception ex) {
            LOG.debug("Optional quickload synonyms.txt file could not be loaded from {}", urlString);
        }

    }

    private void parseGenomeVersionSynonyms(InputStream istream, Multimap<String, String> genomeVersionSynonyms) throws IOException {
        try (Reader reader = new InputStreamReader(istream)) {
            List<CSVRecord> records = getCSVRecordStreamFromTabDelimitedResource(reader);
            records.stream().filter(record -> record.size() >= 2).forEach(record -> {
                genomeVersionSynonyms.putAll(record.get(0), record);
            });
        }
    }

    public void loadSpeciesInfo(String urlString, Set<SpeciesInfo> speciesInfo) {
        try {
            urlString = toExternalForm(urlString);
            urlString += QuickloadConstants.SPECIES_TXT;
            parseSpeciesInfo(dataSourceUtils.getStreamFor(urlString, true), speciesInfo);
        } catch (Exception ex) {
            LOG.debug("Optional species.txt could not be loaded from: {}", urlString);
        }
    }

    private void parseSpeciesInfo(InputStream istream, Set<SpeciesInfo> speciesInfo) throws IOException {
        try (Reader reader = new InputStreamReader(istream)) {
            List<CSVRecord> recordStream = getCSVRecordStreamFromTabDelimitedResource(reader);
            recordStream.forEach(record -> {
                String speciesName = record.get(0);
                String commonName = null;
                String genomeVersionPrefix = null;
                if (record.size() >= 2) {
                    commonName = record.get(1);
                }
                if (record.size() >= 3) {
                    genomeVersionPrefix = record.get(2);
                }
                SpeciesInfo info = new SpeciesInfo(speciesName, commonName, genomeVersionPrefix);
                speciesInfo.add(info);
            });
        }
    }

    public void loadSupportedGenomeVersionInfo(String urlString, Map<String, Optional<String>> supportedGenomeVersionInfo) throws IOException, URISyntaxException {
        try {
            urlString = toExternalForm(urlString);
            urlString += QuickloadConstants.CONTENTS_TXT;
            processContentsTextFile(dataSourceUtils.getStreamFor(urlString, true), supportedGenomeVersionInfo);
        } catch (Exception ex) {
            LOG.error("Could not read contents.txt from: {}", urlString);
            throw ex;
        }
    }

    private void processContentsTextFile(InputStream istream, Map<String, Optional<String>> supportedGenomeVersionInfo) throws IOException {
        try (Reader reader = new InputStreamReader(istream)) {
            List<CSVRecord> recordStream = getCSVRecordStreamFromTabDelimitedResource(reader);
            recordStream.forEach(record -> {
                if (record.size() >= 1) {
                    String genomeName = record.get(0);
                    if (record.size() >= 2) {
                        String description = record.get(1);
                        supportedGenomeVersionInfo.put(genomeName, Optional.of(description));
                    } else {
                        supportedGenomeVersionInfo.put(genomeName, Optional.empty());
                    }
                }
            });
        }
    }

    public Optional<Set<QuickloadFile>> getGenomeVersionData(String quickloadUrl, String genomeVersionName, Map<String, Optional<String>> supportedGenomeVersionInfo) {
        genomeVersionName = getContextRootKey(genomeVersionName, supportedGenomeVersionInfo.keySet()).orElse(genomeVersionName);
        String genomeVersionBaseUrl = getGenomeVersionBaseUrl(quickloadUrl, genomeVersionName);
        String annotsXmlUrl = genomeVersionBaseUrl + QuickloadConstants.ANNOTS_XML;
        try {
            try (InputStream inputStream = dataSourceUtils.getStreamFor(annotsXmlUrl, true)) {
                List<QuickloadFile> annotsFiles = ANNOTS_PARSER.getQuickloadFileList(inputStream);
                if (!annotsFiles.isEmpty()) {
                    return Optional.of(Sets.newLinkedHashSet(annotsFiles));
                } else {
//                    ModalUtils.errorPanel("Could not read annots.xml or this file was empty. Skipping this genome version for quickload site (" + genomeVersionBaseUrl + ")");
                    LOG.error("Could not read annots.xml or this file was empty. Skipping this genome version for quickload site {}", genomeVersionBaseUrl);
                    supportedGenomeVersionInfo.remove(genomeVersionName);
                }
            }
        } catch (Exception ex) {
//            ModalUtils.errorPanel("Could not read annots.xml or this file was empty. Skipping this genome version for quickload site (" + genomeVersionBaseUrl + ")");
            LOG.error("Missing required {} file for genome version {}, skipping this genome version for quickload site {}", ANNOTS_XML, genomeVersionName, genomeVersionBaseUrl, ex);
        }
        return Optional.empty();
    }

    public Optional<Map<String, Integer>> getAssemblyInfo(String quickloadUrl, String genomeVersionName) throws IOException, URISyntaxException {
        String genomeVersionBaseUrl = getGenomeVersionBaseUrl(quickloadUrl, genomeVersionName);
        String genomeTxtUrl = genomeVersionBaseUrl + QuickloadConstants.GENOME_TXT;
        Map<String, Integer> assemblyInfo = Maps.newLinkedHashMap();
        URI uri = new URI(genomeTxtUrl);
        try (Reader reader = new InputStreamReader(dataSourceUtils.getStreamFor(genomeTxtUrl, true));) {
            getCSVRecordStreamFromTabDelimitedResource(reader).stream().filter(record -> record.size() == 2).forEach(record -> {
                assemblyInfo.put(record.get(0), Integer.parseInt(record.get(1)));
            });
        }
        if (!assemblyInfo.isEmpty()) {
            return Optional.of(assemblyInfo);
        }
        return Optional.empty();
    }

    public Optional<String> getContextRootKey(final String genomeVersionName, Set<String> supportedGenomeVersionNames) {
        if (supportedGenomeVersionNames.contains(genomeVersionName)) {
            return Optional.of(genomeVersionName);
        } else {
            Set<String> genomeVersionSynonyms = genomeVersionSynomymService.getSynonyms(genomeVersionName);
            Optional<String> matchingSynonym = genomeVersionSynonyms.stream().filter(syn -> supportedGenomeVersionNames.contains(syn)).findFirst();
            if (matchingSynonym.isPresent()) {
                return Optional.of(matchingSynonym.get());
            }
        }
        return Optional.empty();
    }

    public String getGenomeVersionBaseUrl(String quickloadUrl, String genomeVersionName) {
        final String externalQuickloadUrl = toExternalForm(quickloadUrl);
        final Function<String, String> toGenomeVersionBaseUrl = name -> externalQuickloadUrl + name;
        return toExternalForm(toGenomeVersionBaseUrl.apply(genomeVersionName));
    }

    public static String toExternalForm(String urlString) {
        if (!urlString.endsWith("/")) {
            urlString += "/";
        }
        return urlString.replaceAll(" ", "%20");
    }

    private List<CSVRecord> getCSVRecordStreamFromTabDelimitedResource(final Reader reader) throws IOException {
        Iterable<CSVRecord> records = CSVFormat.TDF
                .withCommentMarker('#')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(true)
                .parse(reader);

        return Lists.newArrayList(records);
    }

    @Reference
    public void setDataSourceUtils(DataSourceUtils dataSourceUtils) {
        this.dataSourceUtils = dataSourceUtils;
    }

    @Reference
    public void setGenomeVersionSynomymService(GenomeVersionSynomymService genomeVersionSynomymService) {
        this.genomeVersionSynomymService = genomeVersionSynomymService;
    }

}
