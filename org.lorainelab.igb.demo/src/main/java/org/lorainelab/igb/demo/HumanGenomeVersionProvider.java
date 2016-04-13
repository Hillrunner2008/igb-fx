package org.lorainelab.igb.demo;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.io.File;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.data.model.ReferenceSequenceProvider;
import org.lorainelab.igb.data.model.util.TwoBitParser;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class HumanGenomeVersionProvider {

    private GenomeVersionRegistry genomeVersionRegistry;
    private final GenomeVersion humanGenome;

    public HumanGenomeVersionProvider() throws Exception {
        ReferenceSequenceProvider twoBitProvider = (ReferenceSequenceProvider) new TwoBitParser(new File(System.getProperty("user.home") + "/sequenceReference/H_sapiens_Dec_2013.2bit"));
        humanGenome = new GenomeVersion("H_sapiens_Dec_2013", "Homo sapiens", twoBitProvider, "Human");
    }

    @Activate
    public void activate() {
        genomeVersionRegistry.getRegisteredGenomeVersions().add(humanGenome);
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }
}
