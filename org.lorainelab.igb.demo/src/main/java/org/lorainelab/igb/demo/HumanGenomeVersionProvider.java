package org.lorainelab.igb.demo;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.lorainelab.igb.cache.api.RemoteFileCacheService;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.data.model.sequence.ReferenceSequenceProvider;
import org.lorainelab.igb.data.model.util.TwoBitParser;
import org.lorainelab.igb.notifications.api.StatusBarNotificationService;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class HumanGenomeVersionProvider {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HumanGenomeVersionProvider.class);
    private GenomeVersionRegistry genomeVersionRegistry;
    private GenomeVersion humanGenome;
    private RemoteFileCacheService cacheService;
    private StatusBarNotificationService notificationService;

    public HumanGenomeVersionProvider() throws Exception {
    }

    @Activate
    public void activate() {
        CompletableFuture<Void> genomeRegistrationTask = CompletableFuture.runAsync(() -> {
            try {
                cacheService.getFilebyUrl(new URL("http://igbquickload.org/H_sapiens_Dec_2013/H_sapiens_Dec_2013.2bit")).ifPresent(cachedFile -> {
                    try {
                        ReferenceSequenceProvider twoBitProvider = (ReferenceSequenceProvider) new TwoBitParser(cachedFile.toString());
                        humanGenome = new GenomeVersion("H_sapiens_Dec_2013", "Homo sapiens", twoBitProvider, "Human");
                        genomeVersionRegistry.getRegisteredGenomeVersions().add(humanGenome);
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                });
            } catch (MalformedURLException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }).whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error(ex.getMessage(), ex);
            }
        });
        notificationService.submitTask(genomeRegistrationTask);
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

}
