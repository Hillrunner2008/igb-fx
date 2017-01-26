package org.lorainelab.igb.quickload;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
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
import org.lorainelab.igb.dataprovider.api.ResourceStatus;
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
        if (!dataProviders.stream().anyMatch(dp -> dp.url().get().equalsIgnoreCase(dataProvider.url().get()))) {
            dataProviders.add(dataProvider);
            initializeDataProvider(dataProvider);
        }
    }

    public void removeDataProvider(DataProvider dataProvider) {
        dataProviders.remove(dataProvider);
        removeAssociatedGenomeVersions(dataProvider);
    }

    public void disableDataProvider(DataProvider dataProvider) {
        removeAssociatedGenomeVersions(dataProvider).whenComplete((u, t) -> {
            dataProvider.setStatus(ResourceStatus.Disabled);
            if (t != null) {
                Throwable ex = (Throwable) t;
                LOG.error(ex.getMessage(), ex);
            }
        });
    }

    public CompletableFuture<Object> removeAssociatedGenomeVersions(DataProvider dataProvider) {
        return CompletableFuture.supplyAsync(() -> {
            dataProvider.getSupportedGenomeVersionNames().stream().forEach(gv -> {
                final Optional<String> sequenceFilePath = dataProvider.getSequenceFilePath(gv);
                if (sequenceFilePath.isPresent()) {
                    List<GenomeVersion> toRemove = genomeVersionRegistry.getRegisteredGenomeVersions().stream().filter(genomeVersion -> genomeVersion.getReferenceSequenceProvider().getPath().equalsIgnoreCase(sequenceFilePath.get())).collect(toList());
                    genomeVersionRegistry.getRegisteredGenomeVersions().removeAll(toRemove);
                }
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
                    final Optional<String> sequenceFilePath = dataProvider.getSequenceFilePath(gv);
                    sequenceFilePath.ifPresent(seqFilePath -> {
                        try {
                            String preferredGenomeVersionName = genomeVersionSynomymService.getPreferredGenomeVersionName(gv).orElse(gv);
                            String speciesName = speciesSynomymService.getPreferredSpeciesName(gv).orElse(gv);
                            if (DataSourceUtilsImpl.resourceAvailable(seqFilePath)) {
                                ReferenceSequenceProvider twoBitProvider = (ReferenceSequenceProvider) new TwoBitParser(seqFilePath, chromosomeSynomymService);
                                Set<Chromosome> chromosomes = Sets.newLinkedHashSet();
                                dataProvider.getAssemblyInfo(preferredGenomeVersionName).ifPresent(chromInfo -> chromInfo.entrySet().stream().forEach(entry -> {
                                    final String name = chromosomeSynomymService.getPreferredChromosomeName(entry.getKey()).orElse(entry.getKey());
                                    chromosomes.add(new Chromosome(name, entry.getValue(), twoBitProvider));
                                }));
                                genomeVersion = new GenomeVersion(preferredGenomeVersionName, speciesName, twoBitProvider, chromosomes, false);
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

    public Optional<DataProvider> getServerFromRequestingUrl(String requestingUrl) {
        SortedMap<Integer, DataProvider> bestMatchMap = Maps.newTreeMap();
        dataProviders.stream().forEach(dataProvider -> {
            bestMatchMap.put(longestSubstr(dataProvider.url().get(), requestingUrl), dataProvider);
        });
        int maxKey = bestMatchMap.lastKey();
        if (maxKey == 0) {
            return Optional.empty();
        } else {
            DataProvider bestMatch = bestMatchMap.get(maxKey);
            String host = null;
            String bestMatchHost = null;
            try {
                URL url = new URL(requestingUrl);
                URL bestMatchUrl = new URL(bestMatch.url().get());
                host = url.getHost();
                bestMatchHost = bestMatchUrl.getHost();
                if (host.equals(bestMatchHost)) {
                    return Optional.of(bestMatchMap.get(maxKey));
                }
            } catch (MalformedURLException ex) {
            }
            return Optional.empty();

        }
    }

    private static int longestSubstr(String first, String second) {
        if (first == null || second == null || first.length() == 0 || second.length() == 0) {
            return 0;
        }

        int maxLen = 0;
        int fl = first.length();
        int sl = second.length();
        int[][] table = new int[fl + 1][sl + 1];

        for (int s = 0; s <= sl; s++) {
            table[0][s] = 0;
        }
        for (int f = 0; f <= fl; f++) {
            table[f][0] = 0;
        }

        for (int i = 1; i <= fl; i++) {
            for (int j = 1; j <= sl; j++) {
                if (first.charAt(i - 1) == second.charAt(j - 1)) {
                    if (i == 1 || j == 1) {
                        table[i][j] = 1;
                    } else {
                        table[i][j] = table[i - 1][j - 1] + 1;
                    }
                    if (table[i][j] > maxLen) {
                        maxLen = table[i][j];
                    }
                }
            }
        }
        return maxLen;
    }
}
