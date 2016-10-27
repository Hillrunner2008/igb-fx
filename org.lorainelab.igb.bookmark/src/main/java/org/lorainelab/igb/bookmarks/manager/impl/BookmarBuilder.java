/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.bookmarks.manager.impl;

import aQute.bnd.annotation.component.Reference;
import java.util.HashMap;
import java.util.Map;
import org.lorainelab.igb.bookmarks.data.BookmarkData;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.GenomeVersionRegistry;

/**
 *
 * @author deva
 */
public class BookmarBuilder {

    private GenomeVersionRegistry genomeVersionRegistry;

    public BookmarkData buidBookmark() {
        BookmarkData bookmarkData = new BookmarkData();
        Map<String, String> data = new HashMap<String, String>();
        GenomeVersion genomeVersion;
        genomeVersionRegistry.getSelectedGenomeVersion().get().ifPresent(gv -> {
            data.put("version", gv.getName().get());
            data.put("species", gv.getSpeciesName().get());
            gv.getSelectedChromosomeProperty().get().ifPresent(chr -> {
                data.put("chromosome", chr.getName());
            });
            data.put("refSeq", gv.getReferenceSequenceProvider().getPath());
        });

        bookmarkData.setDetails(data);
        return bookmarkData;
    }

    @Reference
    public void setGenomeVersionRegistry(GenomeVersionRegistry genomeVersionRegistry) {
        this.genomeVersionRegistry = genomeVersionRegistry;
    }

}
