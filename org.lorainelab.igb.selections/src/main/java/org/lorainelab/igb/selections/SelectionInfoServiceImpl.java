package org.lorainelab.igb.selections;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class SelectionInfoServiceImpl implements SelectionInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(SelectionInfoServiceImpl.class);
    private GenomeVersionRegistry genomeVersionRegistry;
    private final ObjectProperty<Optional<Chromosome>> selectedChromosomeProperty;
    private ObservableSet<CompositionGlyph> selectedGlyphs;

    public SelectionInfoServiceImpl() {
        selectedChromosomeProperty = new SimpleObjectProperty<>(Optional.empty());
        selectedGlyphs = FXCollections.observableSet(Sets.<CompositionGlyph>newTreeSet((glyph1, glyph2) -> {
            return ComparisonChain.start()
                    .compare(glyph1.getLabel(), glyph2.getLabel())
                    .compare(glyph1.getBoundingRect().getMinX(), glyph2.getBoundingRect().getMinX())
                    .compare(glyph1.getBoundingRect().getWidth(), glyph2.getBoundingRect().getWidth())
                    .compare(glyph1.getRenderBoundingRect().getMinY(), glyph2.getRenderBoundingRect().getMinY())
                    .result();
        }));
    }

    @Activate
    public void activate() {
        genomeVersionRegistry.getSelectedGenomeVersion().addListener((obs, oldValue, newValue) -> {
            newValue.ifPresent(genomeVersion -> {
                selectedChromosomeProperty.bind(genomeVersion.getSelectedChromosomeProperty());
            });
        });
    }

    @Override
    public ObjectProperty<Optional<GenomeVersion>> getSelectedGenomeVersion() {
        return genomeVersionRegistry.getSelectedGenomeVersion();
    }

    @Override
    public ObjectProperty<Optional<Chromosome>> getSelectedChromosome() {
        return selectedChromosomeProperty;
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }

    @Override
    public ObservableSet<CompositionGlyph> getSelectedGlyphs() {
        return selectedGlyphs;
    }

    @Deactivate
    public void deactivate() {
        LOG.info("SelectionInfoService deactivated");
    }
}
