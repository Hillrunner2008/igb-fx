package org.lorainelab.igb.filehandler.registry;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandler;
import org.lorainelab.igb.data.model.filehandler.api.FileTypeHandlerRegistry;

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

    @Override
    public Optional<FileTypeHandler> getFileTypeHandler(String path) {
        return fileTypeHandlers.stream().filter(f -> {
            return f.getSupportedExtensions().contains(getFileExtension(path));
        }).findFirst();
    }
    
     private String getFileExtension(String path) {
        String fileExtension = Files.getFileExtension(path);
        if (fileExtension.equalsIgnoreCase("gz")) {
            return Files.getFileExtension(Files.getNameWithoutExtension(path));
        } else {
            return fileExtension;
        }
    }

}
