package org.lorainelab.igb.selections;

import aQute.bnd.annotation.component.Component;
import java.util.Optional;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class SelectionInfoServiceImpl implements SelectionInfoService {

    @Override
    public Optional<GenomeVersion> getSelectedGenomeVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<Chromosome> getSelectedChromosome() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
