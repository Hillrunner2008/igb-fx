/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.filehandler.api;

import java.util.Set;

/**
 *
 * @author dcnorris
 */
public interface FileTypeHandlerRegistry {

    Set<FileTypeHandler> getFileTypeHandlers();

}
