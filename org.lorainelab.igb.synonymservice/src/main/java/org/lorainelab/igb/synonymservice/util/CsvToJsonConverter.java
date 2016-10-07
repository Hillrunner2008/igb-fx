/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymservice.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.JSONPObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
public class CsvToJsonConverter {

    private static final Logger LOG = LoggerFactory.getLogger(CsvToJsonConverter.class);

    public static String loadCsvToJsonString(String filePath) throws IOException {
        InputStream resourceAsStream = CsvToJsonConverter.class.getClassLoader().getResourceAsStream(filePath);
        List<SynonymEntry> data = new CsvLoader().loadSynonyms(resourceAsStream);

//        final OutputStream out = new ByteArrayOutputStream();
        final ObjectMapper mapper = new ObjectMapper();
//        SimpleModule module = new SimpleModule();
//        module.addSerializer(SynonymEntry.class, new CustomSerializer());
//        mapper.registerModule(module);
        String jsonString = mapper.writeValueAsString(data);
        return jsonString;
    }

    private static class DataToJsonConverter {

    }

    private static class CsvLoader {

        InputStream resourceAsStream;
        File csvPath;
        ArrayList<SynonymEntry> csvDatas = new ArrayList<>();
        SynonymEntry data;

        public List<SynonymEntry> loadSynonyms(InputStream istream) throws IOException {
            try (Reader reader = new InputStreamReader(istream)) {
                Iterable<CSVRecord> records = CSVFormat.TDF
                        .withCommentMarker('#')
                        .withIgnoreSurroundingSpaces(true)
                        .withIgnoreEmptyLines(true)
                        .parse(reader);
                for (CSVRecord record : records) {
                    if (StringUtils.isNotEmpty(record.get(0))) {
                        data = new SynonymEntry(record.get(0));
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

    //"[{\"Ailuropoda melanoleuca\":[\"Ailuropoda melanoleuca\",\"Giant panda\",\"A_melanoleuca\",\"ailMel\",\"Ailuropoda_melanoleuca\"]}]"
    public static class CustomSerializer extends StdSerializer<SynonymEntry> {

        public CustomSerializer() {
            this(null);
        }

        public CustomSerializer(Class<SynonymEntry> t) {
            super(t);
        }

        @Override
        public void serialize(SynonymEntry record, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
            gen.writeStartObject();
            gen.writeFieldName(record.getPreferredName());
            gen.writeStartArray();
            record.getSynomyms().forEach(syn -> {
                try {
                    gen.writeString(syn);
                } catch (IOException ex) {
                    LOG.error("failed to writs synonym " + syn + " for " + record.getPreferredName(), ex);
                }
            });
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }
}
