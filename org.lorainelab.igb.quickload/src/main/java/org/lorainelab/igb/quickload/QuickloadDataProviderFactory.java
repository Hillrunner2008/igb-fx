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

    public DataProvider createDataProvider(String url, String name, int loadPriority) {
        url = toExternalForm(url);
        QuickloadDataProvider quickloadDataProvider = new QuickloadDataProvider(url, name, loadPriority);
        quickloadDataProvider.setFileTypeHandlerRegistry(fileTypeHandlerRegistry);
        quickloadDataProvider.setQuickloadUtils(quickloadUtils);
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

}
