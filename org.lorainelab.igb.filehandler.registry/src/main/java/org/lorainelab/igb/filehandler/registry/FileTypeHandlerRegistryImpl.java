package org.lorainelab.igb.filehandler.registry;

import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandlerRegistry;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import java.util.Set;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class FileTypeHandlerRegistryImpl implements FileTypeHandlerRegistry {

    Set<FileTypeHandler> fileTypeHandlers;

    public FileTypeHandlerRegistryImpl() {
        fileTypeHandlers = Sets.newConcurrentHashSet();
    }

    @Reference(optional = false, multiple = true, unbind = "removeFileTypeHandler", dynamic = true)
    public void addFileTypeHandler(FileTypeHandler fileTypeHandler) {
        fileTypeHandlers.add(fileTypeHandler);
    }

    public void removeFileTypeHandler(FileTypeHandler fileTypeHandler) {
        fileTypeHandlers.remove(fileTypeHandler);
    }

    @Override
    public Set<FileTypeHandler> getFileTypeHandlers() {
        return fileTypeHandlers;
    }

}
