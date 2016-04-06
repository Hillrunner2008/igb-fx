package org.lorainelab.igb.data.model.bed;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lorainelab.igb.data.model.Range;
import org.lorainelab.igb.data.model.Strand;

/**
 *
 * @author dcnorris
 */
public class BedParser {

    List<BedFeature> annotations = new ArrayList<>();

    public void parseBedFile() {
        try (InputStream resourceAsStream = BedParser.class.getClassLoader().getResourceAsStream("test.bed");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            Iterator<String> iterator = bufferedReader.lines().iterator();
            while (iterator.hasNext()) {
                String line = iterator.next().trim();
                List<String> fields = Splitter.on("\t").splitToList(line);
                annotations.add(createAnnotation(fields));
            }

        } catch (IOException ex) {
            Logger.getLogger(BedParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private BedFeature createAnnotation(List<String> fields) {
        String chrom = fields.get(0);
        int annotationStart = Integer.parseInt(fields.get(1)); // start field
        int annotationEnd = Integer.parseInt(fields.get(2)); // stop field
        String name = null;
        boolean isForwardStrand;
        if (fields.size() >= 4) {
            name = fields.get(3);
            if (name.equals(".")) {
                //set to empty String
                name = "";
            }
        }
        float score = Float.NEGATIVE_INFINITY;
        if (fields.size() >= 5) {
            score = parseScore(fields.get(4));
        }
        if (fields.size() >= 6) {
            if (fields.get(5).equals(".")) {
                isForwardStrand = (annotationStart <= annotationEnd);
            } else {
                isForwardStrand = fields.get(5).equals("+");
            }
        } else {
            isForwardStrand = (annotationStart <= annotationEnd);
        }
        int min = Math.min(annotationStart, annotationEnd);
        int max = Math.max(annotationStart, annotationEnd);
        int thickStart = -1;
        int thickEnd = -1;
        if (fields.size() >= 8) {
            thickStart = Integer.parseInt(fields.get(6));
            thickEnd = Integer.parseInt(fields.get(7));
        }
        String itemRgb;
        if (fields.size() >= 9) {
            itemRgb = fields.get(8);
        }
        int exonCount = -1;
        int[] exonSizes = null;
        int[] exonStartPositions = null;
        if (fields.size() >= 12) {
            exonCount = Integer.parseInt(fields.get(9));
            exonSizes = parseIntArray(fields.get(10));
            exonStartPositions = parseIntArray(fields.get(11));
        }
        String description = "";
        if (fields.size() >= 14) {
            description = fields.get(13);
        }
        Range annotationRange = new Range(annotationStart, annotationEnd);
        BedFeature bedFeature = new BedFeature(chrom, annotationRange, isForwardStrand ? Strand.POSITIVE : Strand.NEGATIVE);
        bedFeature.setId(name);
        bedFeature.setCdsStart(thickStart);
        bedFeature.setCdsEnd(thickEnd);
        bedFeature.setLabel(name);
        bedFeature.setScore(score + "");
        bedFeature.setDescription(description);
        for (int i = 0; i < exonCount; i++) {
            final int exonStart = exonStartPositions[i];
            final int exonEnd = exonStartPositions[i] + exonSizes[i];
            bedFeature.getExons().add(new Range(exonStart, exonEnd));
        }
        return bedFeature;
    }

    public static int[] parseIntArray(String intArray) {
        if (Strings.isNullOrEmpty(intArray)) {
            return new int[0];
        }
        List<String> intstrings = Splitter.on(",").omitEmptyStrings().splitToList(intArray);
        int count = intstrings.size();
        int[] results = new int[count];
        for (int i = 0; i < count; i++) {
            int val = Integer.parseInt(intstrings.get(i));
            results[i] = val;
        }
        return results;
    }

    public static boolean checkRange(int start, int end, int min, int max) {
        return !(end < min || start > max);
    }

    public static float parseScore(String s) {
        if (s == null || s.length() == 0 || s.equals(".") || s.equals("-")) {
            return 0.0f;
        }
        return Float.parseFloat(s);
    }

    public List<BedFeature> getAnnotations() {
        parseBedFile();
        return annotations;
    }

}
