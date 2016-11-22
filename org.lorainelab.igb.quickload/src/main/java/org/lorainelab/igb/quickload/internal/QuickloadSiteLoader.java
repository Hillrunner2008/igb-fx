package org.lorainelab.igb.quickload.internal;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.lorainelab.igb.dataprovider.api.BaseDataProvider;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.IS_EDITABLE;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.LOAD_PRIORITY;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.PRIMARY_URL;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.PROVIDER_NAME;
import org.lorainelab.igb.preferences.PreferenceUtils;
import org.lorainelab.igb.quickload.QuickloadDataProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class QuickloadSiteLoader {

    private static final Logger LOG = LoggerFactory.getLogger(QuickloadSiteLoader.class);
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private IgbPreferences defaultPreferences;
    private QuickloadDataProviderFactory quickloadDataProviderFactory;

    public QuickloadSiteLoader() {
        initDefaultPreferences();
    }

    private void initDefaultPreferences() {
        if (defaultPreferences == null) {
            try (Reader reader = new InputStreamReader(QuickloadSiteLoader.class.getClassLoader().getResourceAsStream("igbDefaultPrefs.json"));) {
                defaultPreferences = gson.fromJson(reader, JsonWrapper.class).getPrefs();
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    @Activate
    public void activate() {
        loadDefaultQuickloadSites();
        loadUserCreatedQuickloadSites();
    }

    private void loadDefaultQuickloadSites() {
        defaultPreferences.getDataProviders()
                .stream()
                .forEach(dataProvider -> quickloadDataProviderFactory.createDataProvider(dataProvider.url, dataProvider.name, false, dataProvider.loadPriority));
    }

    @Reference
    public void setQuickloadDataProviderFactory(QuickloadDataProviderFactory quickloadDataProviderFactory) {
        this.quickloadDataProviderFactory = quickloadDataProviderFactory;
    }

    private void loadUserCreatedQuickloadSites() {
        Preferences quickloadRootNode = PreferenceUtils.getClassPrefsNode(BaseDataProvider.class);
        try {
            Arrays.stream(quickloadRootNode.childrenNames())
                    .map(nodeName -> quickloadRootNode.node(nodeName))
                    .forEach(node -> {
                        initializeQuickloadSiteFromPreferencesNode(node);
                    });
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void initializeQuickloadSiteFromPreferencesNode(Preferences node) {
        boolean isEditable = node.getBoolean(IS_EDITABLE, true);
        if (isEditable) {
            String name = node.get(PROVIDER_NAME, "");
            String url = node.get(PRIMARY_URL, "");
            int loadPriority = node.getInt(LOAD_PRIORITY, 5);
            quickloadDataProviderFactory.createDataProvider(url, name, loadPriority);
        }
    }

}
