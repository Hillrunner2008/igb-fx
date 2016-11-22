package org.lorainelab.igb.quickload;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.lorainelab.igb.cache.api.RemoteFileCacheService;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.data.model.sequence.ReferenceSequenceProvider;
import org.lorainelab.igb.data.model.util.DataSourceUtilsImpl;
import org.lorainelab.igb.data.model.util.TwoBitParser;
import org.lorainelab.igb.dataprovider.api.DataProvider;
import org.lorainelab.igb.dataprovider.model.DataContainer;
import org.lorainelab.igb.notifications.api.StatusBarNotificationService;
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
    private RemoteFileCacheService cacheService;
    private StatusBarNotificationService notificationService;
    private static final Set<DataProvider> dataProviders = Sets.newConcurrentHashSet();
    private GenomeVersionSynomymService genomeVersionSynomymService;
    private SpeciesSynomymService speciesSynomymService;
    private ChromosomeSynomymService chromosomeSynomymService;

    public QuickloadSiteManager() throws Exception {
    }

    @Activate
    public void activate() {
        CompletableFuture<Void> initializeTask = CompletableFuture.runAsync(() -> {
            try {

//                cacheService.getFilebyUrl(new URL("http://igbquickload.org/H_sapiens_Dec_2013/H_sapiens_Dec_2013.2bit")).ifPresent(cachedFile -> {
//                    try {
//                        ReferenceSequenceProvider twoBitProvider = (ReferenceSequenceProvider) new TwoBitParser(cachedFile.toString());
//                        humanGenome = new GenomeVersion("H_sapiens_Dec_2013", "Homo sapiens", twoBitProvider, "Human");
//                        genomeVersionRegistry.getRegisteredGenomeVersions().add(humanGenome);
//                    } catch (Exception ex) {
//                        LOG.error(ex.getMessage(), ex);
//                    }
//                });
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }).whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error(ex.getMessage(), ex);
            }
        });
//        notificationService.submitTask(genomeRegistrationTask);
    }

    @Reference(optional = true, multiple = true, dynamic = true, unbind = "removeDataProvider")
    public void addDataProvider(DataProvider dataProvider) {
        dataProviders.add(dataProvider);
        initializeDataProvider(dataProvider);
    }

    private void initializeDataProvider(DataProvider dataProvider) {
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
                                genomeVersion = new GenomeVersion(preferredGenomeVersionName, speciesName, twoBitProvider, speciesName, false);
                                genomeVersionRegistry.getRegisteredGenomeVersions().add(genomeVersion);
                                genomeVersion.getDataContainers().add(new DataContainer(genomeVersion, dataProvider));
                            } else {
                                genomeVersionRegistry.getRegisteredGenomeVersions().stream()
                                        .filter(genomeVersion -> genomeVersion.name().get().equalsIgnoreCase(preferredGenomeVersionName))
                                        .findFirst().ifPresent(match -> {
                                            match.getDataContainers().add(new DataContainer(match, dataProvider));
                                        });
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

    public void removeDataProvider(DataProvider dataProvider) {
        dataProviders.remove(dataProvider);
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }

    @Reference
    public void setRemoteFileCacheService(RemoteFileCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Reference
    public void setStatusBarNotificationService(StatusBarNotificationService notificationService) {
        this.notificationService = notificationService;
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
