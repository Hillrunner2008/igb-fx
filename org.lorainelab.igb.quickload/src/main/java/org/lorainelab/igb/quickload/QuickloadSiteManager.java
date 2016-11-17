package org.lorainelab.igb.quickload;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import static java.util.stream.Collectors.toList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.data.model.sequence.ReferenceSequenceProvider;
import org.lorainelab.igb.data.model.util.DataSourceUtilsImpl;
import org.lorainelab.igb.data.model.util.TwoBitParser;
import org.lorainelab.igb.dataprovider.api.DataProvider;
import org.lorainelab.igb.synonymservice.ChromosomeSynomymService;
import org.lorainelab.igb.synonymservice.GenomeVersionSynomymService;
import org.lorainelab.igb.synonymservice.SpeciesSynomymService;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = QuickloadSiteManager.class)
public class QuickloadSiteManager {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(QuickloadSiteManager.class);
    private GenomeVersionRegistry genomeVersionRegistry;
    private GenomeVersion genomeVersion;
    private final ObservableSet<DataProvider> dataProviders;
    private GenomeVersionSynomymService genomeVersionSynomymService;
    private SpeciesSynomymService speciesSynomymService;
    private ChromosomeSynomymService chromosomeSynomymService;

    public QuickloadSiteManager() throws Exception {
        dataProviders = FXCollections.observableSet(Sets.newConcurrentHashSet());
    }

    @Activate
    public void activate() {
    }

    @Reference(optional = true, multiple = true, dynamic = true, unbind = "removeDataProvider")
    public void addDataProvider(DataProvider dataProvider) {
        dataProviders.add(dataProvider);
        initializeDataProvider(dataProvider);
    }

    public void removeDataProvider(DataProvider dataProvider) {
        dataProviders.remove(dataProvider);
        removeAssociatedGenomeVersions(dataProvider);
    }

    public void removeAssociatedGenomeVersions(DataProvider dataProvider) {
        CompletableFuture.supplyAsync(() -> {
            dataProvider.getSupportedGenomeVersionNames().stream().forEach(gv -> {
                final Optional<URI> sequenceFilePath = dataProvider.getSequenceFilePath(gv);
                sequenceFilePath.ifPresent(seqFilePath -> {
                    List<GenomeVersion> toRemove = genomeVersionRegistry.getRegisteredGenomeVersions().stream().filter(genomeVersion -> genomeVersion.getReferenceSequenceProvider().getPath().equals(seqFilePath))
                            .collect(toList());
                    genomeVersionRegistry.getRegisteredGenomeVersions().removeAll(toRemove);
                });
            });

            return null;
        }).whenComplete((u, t) -> {
            if (t != null) {
                Throwable ex = (Throwable) t;
                LOG.error(ex.getMessage(), ex);
            }
        });
    }

    public void initializeDataProvider(DataProvider dataProvider) {
        CompletableFuture.supplyAsync(() -> {
            dataProvider.initialize();
            return null;
        }).whenComplete((u, t) -> {
            if (t != null) {
                Throwable ex = (Throwable) t;
                LOG.error(ex.getMessage(), ex);
            } else {
                initializeSupportedGenomes(dataProvider);
            }
        });
    }

    private void initializeSupportedGenomes(DataProvider dataProvider) {
        dataProvider.getSupportedGenomeVersionNames().stream().forEach(gv -> {
            CompletableFuture<Void> initializeTask = CompletableFuture.runAsync(() -> {
                try {
                    final Optional<URI> sequenceFilePath = dataProvider.getSequenceFilePath(gv);
                    sequenceFilePath.ifPresent(seqFilePath -> {
                        try {
                            String preferredGenomeVersionName = genomeVersionSynomymService.getPreferredGenomeVersionName(gv).orElse(gv);
                            String speciesName = speciesSynomymService.getPreferredSpeciesName(gv).orElse(gv);
                            if (DataSourceUtilsImpl.resourceAvailable(seqFilePath.toURL())) {
                                ReferenceSequenceProvider twoBitProvider = (ReferenceSequenceProvider) new TwoBitParser(seqFilePath.toURL().toExternalForm(), chromosomeSynomymService);
                                Set<Chromosome> chromosomes = Sets.newLinkedHashSet();
                                dataProvider.getAssemblyInfo(preferredGenomeVersionName).ifPresent(chromInfo -> chromInfo.entrySet().stream().forEach(entry -> {
                                    chromosomes.add(new Chromosome(entry.getKey(), entry.getValue(), twoBitProvider));
                                }));

                                genomeVersion = new GenomeVersion(preferredGenomeVersionName, speciesName, twoBitProvider, chromosomes);
                                genomeVersionRegistry.getRegisteredGenomeVersions().add(genomeVersion);
                            }
                        } catch (Exception ex) {
                            LOG.error(ex.getMessage(), ex);
                        }
                    });
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }).whenComplete((result, ex) -> {
                if (ex != null) {
                    LOG.error(ex.getMessage(), ex);
                }
            });
        });
    }

    public ObservableSet<DataProvider> getDataProviders() {
        return dataProviders;
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }

    @Reference
    public void setGenomeVersionSynomymService(GenomeVersionSynomymService genomeVersionSynomymService) {
        this.genomeVersionSynomymService = genomeVersionSynomymService;
    }

    @Reference
    public void setSpeciesSynomymService(SpeciesSynomymService speciesSynomymService) {
        this.speciesSynomymService = speciesSynomymService;
    }

    @Reference
    public void setChromosomeSynomymService(ChromosomeSynomymService chromosomeSynomymService) {
        this.chromosomeSynomymService = chromosomeSynomymService;
    }
}
