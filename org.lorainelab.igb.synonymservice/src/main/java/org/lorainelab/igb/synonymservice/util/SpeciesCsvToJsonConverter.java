/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta
 */
public class SpeciesCsvToJsonConverter {

    private static final Logger LOG = LoggerFactory.getLogger(CsvToJsonConverter.class);

    public static String loadSpeciesCsvToJsonString(String filePath) throws IOException {
        InputStream resourceAsStream = CsvToJsonConverter.class.getClassLoader().getResourceAsStream(filePath);
        List<SynonymEntry> data = new CsvLoader().loadSynonyms(resourceAsStream);

        final ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(data);
        return jsonString;
    }

    private static class CsvLoader {

        InputStream resourceAsStream;
        File csvPath;
        ArrayList<SynonymEntry> csvDatas = new ArrayList<>();
        SpeciesSynonymEntry data;

        public List<SynonymEntry> loadSynonyms(InputStream istream) throws IOException {
            try (Reader reader = new InputStreamReader(istream)) {
                Iterable<CSVRecord> records = CSVFormat.TDF
                        .withCommentMarker('#')
                        .withIgnoreSurroundingSpaces(true)
                        .withIgnoreEmptyLines(true)
                        .parse(reader);
                for (CSVRecord record : records) {
                    if (StringUtils.isNotEmpty(record.get(0))) {
                        data = new SpeciesSynonymEntry(record.get(0));
                    }
                    if (StringUtils.isNotEmpty(record.get(1))) {
                        data.setCommonName(record.get(1));
                    }
                    Set<String> row = new LinkedHashSet();
                    for (String entry : record) {
                        if (StringUtils.isNotEmpty(entry)) {
                            row.add(entry);
                        }
                    }
                    data.setSynomyms(row);
                    csvDatas.add(data);
                }
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
                t.printStackTrace();
            }
            return csvDatas;
        }
    }
}
