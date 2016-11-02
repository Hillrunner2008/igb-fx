package org.lorainelab.igb.data.model.filehandler.api;

import com.google.common.collect.Sets;
import java.util.Set;

/**
 * Implementations of IndexedFileHandler will register index extensions to be checked
 * before FileTypeHandlers. If found, the IndexedFileHandler will be used.
 *
 * @author dcnorris
 */
public interface IndexedFileHandler extends FileTypeHandler {

    @Override
    default Set<DataType> getDataTypes() {
        return Sets.newHashSet(DataType.ANNOTATION, DataType.ALIGNMENT, DataType.GRAPH);
    }

}
