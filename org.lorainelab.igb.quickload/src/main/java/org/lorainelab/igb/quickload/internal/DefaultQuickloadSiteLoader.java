package org.lorainelab.igb.quickload.internal;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStreamReader;
import java.io.Reader;
import org.lorainelab.igb.quickload.QuickloadDataProviderFactory;
import org.lorainelab.igb.quickload.QuickloadSiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class DefaultQuickloadSiteLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultQuickloadSiteLoader.class);
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private IgbPreferences defaultPreferences;
    private QuickloadDataProviderFactory quickloadDataProviderFactory;
    private QuickloadSiteManager quickloadSiteManager;

    public DefaultQuickloadSiteLoader() {
        initDefaultPreferences();
    }

    private void initDefaultPreferences() {
        if (defaultPreferences == null) {
            try (Reader reader = new InputStreamReader(DefaultQuickloadSiteLoader.class.getClassLoader().getResourceAsStream("igbDefaultPrefs.json"));) {
                defaultPreferences = gson.fromJson(reader, JsonWrapper.class).getPrefs();
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    @Activate
    public void activate() {
        defaultPreferences.getDataProviders()
                .stream()
                .map(dataProvider -> quickloadDataProviderFactory.createDataProvider(dataProvider.url, dataProvider.name, dataProvider.loadPriority))
                .forEach(quickloadSiteManager::addDataProvider);
    }

    @Reference
    public void setQuickloadDataProviderFactory(QuickloadDataProviderFactory quickloadDataProviderFactory) {
        this.quickloadDataProviderFactory = quickloadDataProviderFactory;
    }

    @Reference
    public void setQuickloadSiteManager(QuickloadSiteManager quickloadSiteManager) {
        this.quickloadSiteManager = quickloadSiteManager;
    }

}
