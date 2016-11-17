package org.lorainelab.igb.filehandler.registry;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public List<String> getAllSupportedFileExtensions() {
        List<String> supportedExtensions = new ArrayList<>();
        fileTypeHandlers.stream().forEach(handler -> {
            handler.getSupportedExtensions().forEach(ext -> {
                supportedExtensions.add(ext);
                supportedExtensions.add(ext + GZIPPED_EXT);
                supportedExtensions.add(ext + GZIPPED_EXT_ALT);
                supportedExtensions.add(ext + GZIPPED_EXT_ALT_2);
                supportedExtensions.add(ext + ZIPPED_EXT);
                supportedExtensions.add(ext + BZ_2);
            });
        });
        return supportedExtensions;
    }
    private static final String GZIPPED_EXT = ".gz";
    private static final String GZIPPED_EXT_ALT = ".z";
    private static final String GZIPPED_EXT_ALT_2 = ".gzip";
    private static final String ZIPPED_EXT = ".zip";
    private static final String BZ_2 = ".bz2";

}
