package org.lorainelab.igb.quickload;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandlerRegistry;
import org.lorainelab.igb.dataprovider.api.BaseDataProvider;
import org.lorainelab.igb.dataprovider.api.DataProvider;
import org.lorainelab.igb.dataprovider.api.ResourceStatus;
import static org.lorainelab.igb.dataprovider.api.ResourceStatus.Initialized;
import org.lorainelab.igb.dataprovider.api.SpeciesInfo;
import static org.lorainelab.igb.quickload.QuickloadConstants.GENOME_TXT;
import org.lorainelab.igb.quickload.model.QuickloadFile;
import org.lorainelab.igb.quickload.util.QuickloadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class QuickloadDataProvider extends BaseDataProvider implements DataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(QuickloadDataProvider.class);
    private final Set<SpeciesInfo> speciesInfo;
    private final SetMultimap<String, String> genomeVersionSynonyms;
    private final Map<String, Optional<String>> supportedGenomeVersionInfo;
    private final Map<String, Optional<Multimap<String, String>>> chromosomeSynonymReference;
    private FileTypeHandlerRegistry fileTypeHandlerRegistry;
    private QuickloadUtils quickloadUtils;

    QuickloadDataProvider(String url, String name, boolean isEditable, int loadPriority) {
        super(QuickloadUtils.toExternalForm(url), name, isEditable, loadPriority);
        supportedGenomeVersionInfo = Maps.newConcurrentMap();
        speciesInfo = Sets.newHashSet();
        genomeVersionSynonyms = HashMultimap.create();
        chromosomeSynonymReference = Maps.newHashMap();
    }

//    QuickloadDataProvider(String url, String name, String mirrorUrl, int loadPriority) {
//        super(QuickloadUtils.toExternalForm(url), name, QuickloadUtils.toExternalForm(mirrorUrl), loadPriority);
//        supportedGenomeVersionInfo = Maps.newHashMap();
//        speciesInfo = Sets.newHashSet();
//        genomeVersionSynonyms = HashMultimap.create();
//        chromosomeSynonymReference = Maps.newHashMap();
//    }
    @Override
    public void initialize() {
        if (status == ResourceStatus.Disabled) {
            return;
        }
        LOG.info("Initializing Quickload Server {}", url().get());
        populateSupportedGenomeVersionInfo();
        loadOptionalQuickloadFiles();
        if (status != ResourceStatus.NotResponding) {
            setStatus(Initialized);
        }
    }

    @Override
    protected void disable() {
        supportedGenomeVersionInfo.clear();
        speciesInfo.clear();
        genomeVersionSynonyms.clear();
        chromosomeSynonymReference.clear();
    }

    private void loadOptionalQuickloadFiles() {
        quickloadUtils.loadGenomeVersionSynonyms(url().get(), genomeVersionSynonyms);
        quickloadUtils.loadSpeciesInfo(url().get(), speciesInfo);
    }

    private void populateSupportedGenomeVersionInfo() {
        try {
            quickloadUtils.loadSupportedGenomeVersionInfo(url().get(), supportedGenomeVersionInfo);
        } catch (IOException | URISyntaxException ex) {
//            if (!useMirror && getMirrorUrl().isPresent()) {
//                useMirror = true;
//                initialize();
//            } else {
            LOG.error("Missing required quickload file, or could not reach source. This quickloak source will be disabled for this session.");
            status = ResourceStatus.NotResponding;
//                useMirror = false; //reset to default url since mirror may have been tried
//            }
        }
    }

    @Override
    public Optional<Map<String, Integer>> getAssemblyInfo(String genomeVersionName) {
        genomeVersionName = quickloadUtils.getContextRootKey(genomeVersionName, supportedGenomeVersionInfo.keySet()).orElse(genomeVersionName);
        try {
            return quickloadUtils.getAssemblyInfo(url.get(), genomeVersionName);
        } catch (Exception ex) {
            LOG.error("Missing required {} file for genome version {}, skipping this genome version for quickload site {}", GENOME_TXT, genomeVersionName, url().get());
        }
        return Optional.empty();
    }

    @Override
    public Set<String> getSupportedGenomeVersionNames() {
        return supportedGenomeVersionInfo.keySet();
    }

    @Override
    public Optional<String> getGenomeVersionDescription(String genomeVersionName) {
        genomeVersionName = quickloadUtils.getContextRootKey(genomeVersionName, supportedGenomeVersionInfo.keySet()).orElse(genomeVersionName);
        if (supportedGenomeVersionInfo.containsKey(genomeVersionName)) {
            return supportedGenomeVersionInfo.get(genomeVersionName);
        }
        return Optional.empty();
    }

    @Override
    public Optional<SetMultimap<String, String>> getGenomeVersionSynonyms() {
        return Optional.of(genomeVersionSynonyms);
    }

    @Override
    public Set<DataSet> getAvailableDataSets(String genomeVersionName) {
        String internalgenomeVersionName = quickloadUtils.getContextRootKey(genomeVersionName, supportedGenomeVersionInfo.keySet()).orElse(genomeVersionName);
        final Optional<Set<QuickloadFile>> genomeVersionData = quickloadUtils.getGenomeVersionData(url().get(), internalgenomeVersionName, supportedGenomeVersionInfo);
        if (genomeVersionData.isPresent()) {
            Set<QuickloadFile> versionFiles = genomeVersionData.get();
            LinkedHashSet<DataSet> dataSets = Sets.newLinkedHashSet();

            List<QuickloadFile> missingNameAttribute = versionFiles.stream().filter(file -> Strings.isNullOrEmpty(file.getName())).collect(Collectors.toList());
            if (!missingNameAttribute.isEmpty()) {
                LOG.error("The " + genomeVersionName + " genome contains some missing name attributes in its annots.xml file on the quickload site (" + url().get() + ")");
//                ModalUtils.errorPanel("The " + genomeVersionName + " genome contains some missing name attributes in its annots.xml file on the quickload site (" + url().get() + ")");
            }
            versionFiles.stream().filter(file -> !Strings.isNullOrEmpty(file.getName())).forEach((file) -> {
                try {
                    URI uri;
                    if (!file.getName().startsWith("http")) {
                        uri = new URI(url().get() + internalgenomeVersionName + "/" + file.getName());
                    } else {
                        uri = new URI(file.getName());
                    }
                    final String toExternalForm = uri.toURL().toExternalForm();
                    fileTypeHandlerRegistry.getFileTypeHandler(toExternalForm).ifPresent(fileTypeHandler -> {
                        DataSet dataSet = new DataSet(toExternalForm, toExternalForm, fileTypeHandler);
                        dataSets.add(dataSet);
                    });
                } catch (URISyntaxException | MalformedURLException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            });
            return dataSets;
        } else {
            return Sets.newLinkedHashSet();
        }
    }

    @Override
    public Optional<String> getSequenceFilePath(String name) {
        final String genomeVersionName = quickloadUtils.getContextRootKey(name, supportedGenomeVersionInfo.keySet()).orElse(name);
        final String sequenceFileLocation = quickloadUtils.getGenomeVersionBaseUrl(url().get(), genomeVersionName) + genomeVersionName + ".2bit";
        return Optional.of(sequenceFileLocation);
    }

    public void setFileTypeHandlerRegistry(FileTypeHandlerRegistry fileTypeHandlerRegistry) {
        this.fileTypeHandlerRegistry = fileTypeHandlerRegistry;
    }

    public void setQuickloadUtils(QuickloadUtils quickloadUtils) {
        this.quickloadUtils = quickloadUtils;
    }

}
