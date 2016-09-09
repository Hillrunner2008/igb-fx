package org.lorainelab.igb.filehandler.registry;

import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandlerRegistry;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class FileTypeHandlerRegistryImpl implements FileTypeHandlerRegistry {

    ObservableSet<FileTypeHandler> fileTypeHandlers;

    public FileTypeHandlerRegistryImpl() {
        fileTypeHandlers = FXCollections.observableSet(Sets.newConcurrentHashSet());
    }

    @Reference(optional = false, multiple = true, unbind = "removeFileTypeHandler", dynamic = true)
    public void addFileTypeHandler(FileTypeHandler fileTypeHandler) {
        fileTypeHandlers.add(fileTypeHandler);
    }

    public void removeFileTypeHandler(FileTypeHandler fileTypeHandler) {
        fileTypeHandlers.remove(fileTypeHandler);
    }

    @Override
    public ObservableSet<FileTypeHandler> getFileTypeHandlers() {
        return fileTypeHandlers;
    }

}
