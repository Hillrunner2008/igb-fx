package org.lorainelab.igb.menu.customgenome;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;
import org.lorainelab.igb.data.model.sequence.ReferenceSequenceProvider;
import org.lorainelab.igb.data.model.util.TwoBitParser;
import static org.lorainelab.igb.menu.customgenome.CustomGenomePrefKeys.REFERENCE_PROVIDER_URL;
import static org.lorainelab.igb.menu.customgenome.CustomGenomePrefKeys.SPECIES_NAME;
import static org.lorainelab.igb.menu.customgenome.CustomGenomePrefKeys.UUID;
import static org.lorainelab.igb.menu.customgenome.CustomGenomePrefKeys.VERSION_NAME;
import org.lorainelab.igb.preferences.PreferenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = CustomGenomePersistenceManager.class)
public class CustomGenomePersistenceManager {

    private static final Logger LOG = LoggerFactory.getLogger(CustomGenomePersistenceManager.class);
    private static HashFunction md5HashFunction;
    private Preferences modulePreferencesNode;
    private GenomeVersionRegistry genomeVersionRegistry;
    
    public CustomGenomePersistenceManager() {
        md5HashFunction = Hashing.md5();
        modulePreferencesNode = PreferenceUtils.getPackagePrefsNode(CustomGenomePersistenceManager.class);
    }

    @Activate
    public void activate() {
        try {
            Arrays.stream(modulePreferencesNode.childrenNames())
                    .map(nodeName -> modulePreferencesNode.node(nodeName))
                    .forEach(node -> {
                        initializeCustomGenomeFromPreferencesNode(node);
                    });
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }

    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }
    
    private void initializeCustomGenomeFromPreferencesNode(Preferences node) {
        String speciesName = node.get(SPECIES_NAME, "");
        String versionName = node.get(VERSION_NAME, "");
        String sequenceFileUrl = node.get(REFERENCE_PROVIDER_URL, "");
        UUID uuid = java.util.UUID.fromString(node.get(UUID, "")); 
        if (!Strings.isNullOrEmpty(speciesName)
                || !Strings.isNullOrEmpty(versionName)
                || !Strings.isNullOrEmpty(sequenceFileUrl)
                || uuid != null) {
            try {
                ReferenceSequenceProvider referenceSequenceProvider = (ReferenceSequenceProvider) new TwoBitParser(sequenceFileUrl);
                GenomeVersion customGenome = new GenomeVersion(versionName, speciesName, referenceSequenceProvider, versionName,uuid);
                genomeVersionRegistry.getRegisteredGenomeVersions().add(customGenome);
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    private static String md5Hash(String url) {
        HashCode hc = md5HashFunction.newHasher().putString(url, Charsets.UTF_8).hash();
        return hc.toString();
    }

    void persistCustomGenome(GenomeVersion customGenome) {
        String speciesName = customGenome.getSpeciesName().get();
        String versionName = customGenome.getName().get();
        String sequenceFileUrl = customGenome.getReferenceSequenceProvider().getPath();
        String uuid = customGenome.getUuid().toString();
        String nodeName = md5Hash(uuid);
        Preferences node = modulePreferencesNode.node(nodeName);
        node.put(SPECIES_NAME, speciesName);
        node.put(VERSION_NAME, versionName);
        node.put(REFERENCE_PROVIDER_URL, sequenceFileUrl);
        node.put(UUID, uuid);
    }
    
    void deleteCustomGenome(GenomeVersion customGenome){     
        String uuid = customGenome.getUuid().toString();
        String nodeName = md5Hash(uuid);
        Preferences node = modulePreferencesNode.node(nodeName);
        try {            
            node.removeNode();
        } catch (BackingStoreException ex) {
            java.util.logging.Logger.getLogger(CustomGenomePersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
