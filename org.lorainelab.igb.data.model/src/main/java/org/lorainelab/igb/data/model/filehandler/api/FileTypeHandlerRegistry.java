/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.filehandler.api;

import java.util.List;
import java.util.Optional;
import javafx.collections.ObservableSet;

/**
 *
 * @author dcnorris
 */
public interface FileTypeHandlerRegistry {

    ObservableSet<FileTypeHandler> getFileTypeHandlers();

    Optional<FileTypeHandler> getFileTypeHandler(String path);

    List<String> getAllSupportedFileExtensions();

}
