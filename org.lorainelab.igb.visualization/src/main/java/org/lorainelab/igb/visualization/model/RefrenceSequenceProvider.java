package org.lorainelab.igb.visualization.model;

import aQute.bnd.annotation.component.Component;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lorainelab.igb.visualization.util.TwoBitParser;

@Component(immediate = true, provide = RefrenceSequenceProvider.class)
public class RefrenceSequenceProvider {

    private String referenceDna;

    public RefrenceSequenceProvider() {
        loadSequence();
    }

    public String getReferenceDna() {
        return referenceDna;
    }

    private void loadSequence() {
        try (TwoBitParser parser = new TwoBitParser(new File(System.getProperty("user.home") + "/sequenceReference/H_sapiens_Dec_2013.2bit"))) {
            final String[] sequenceNames = parser.getSequenceNames();
            Arrays.sort(sequenceNames);
            parser.setCurrentSequence(sequenceNames[0]);
            referenceDna = parser.loadFragment(0, parser.available());
        } catch (Exception ex) {
            Logger.getLogger(Track.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
