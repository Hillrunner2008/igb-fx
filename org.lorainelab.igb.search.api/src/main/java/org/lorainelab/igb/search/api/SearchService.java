/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.search.api;

import java.util.List;
import org.lorainelab.igb.search.api.model.Document;

/**
 *
 * @author jeckstei
 */
public interface SearchService {
    public List<Document> search(String query);
    public void index(Document document);
}
