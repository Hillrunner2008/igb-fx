package org.lorainelab.igb.quickload;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandlerRegistry;
import org.lorainelab.igb.dataprovider.api.DataProvider;
import org.lorainelab.igb.quickload.util.QuickloadUtils;
import static org.lorainelab.igb.quickload.util.QuickloadUtils.toExternalForm;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = QuickloadDataProviderFactory.class)
public class QuickloadDataProviderFactory {

    private FileTypeHandlerRegistry fileTypeHandlerRegistry;
    private QuickloadUtils quickloadUtils;
    private QuickloadSiteManager quickloadSiteManager;

    public DataProvider createDataProvider(String url, String name, boolean isEditable, int loadPriority) {
        url = toExternalForm(url);
        QuickloadDataProvider quickloadDataProvider = new QuickloadDataProvider(url, name, isEditable, loadPriority);
        quickloadDataProvider.setFileTypeHandlerRegistry(fileTypeHandlerRegistry);
        quickloadDataProvider.setQuickloadUtils(quickloadUtils);
        quickloadSiteManager.addDataProvider(quickloadDataProvider);
        return quickloadDataProvider;
    }

    public DataProvider createDataProvider(String url, String name, int loadPriority) {
        url = toExternalForm(url);
        QuickloadDataProvider quickloadDataProvider = new QuickloadDataProvider(url, name, true, loadPriority);
        quickloadDataProvider.setFileTypeHandlerRegistry(fileTypeHandlerRegistry);
        quickloadDataProvider.setQuickloadUtils(quickloadUtils);
        quickloadSiteManager.addDataProvider(quickloadDataProvider);
        return quickloadDataProvider;
    }

    @Reference
    public void setFileTypeHandlerRegistry(FileTypeHandlerRegistry fileTypeHandlerRegistry) {
        this.fileTypeHandlerRegistry = fileTypeHandlerRegistry;
    }

    @Reference
    public void setQuickloadUtils(QuickloadUtils quickloadUtils) {
        this.quickloadUtils = quickloadUtils;
    }

    @Reference
    public void setQuickloadSiteManager(QuickloadSiteManager quickloadSiteManager) {
        this.quickloadSiteManager = quickloadSiteManager;
    }

}
