/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.search.api;

import java.net.URI;
import java.util.List;
import org.lorainelab.igb.search.api.model.Document;
import org.lorainelab.igb.search.api.model.IndexIdentity;

/**
 *
 * @author jeckstei
 */
public interface SearchService {
    public List<Document> search(String query, IndexIdentity index);
    public void index(List<Document> document, IndexIdentity index);
    public void clearIndex(IndexIdentity index);
    public void deleteAll();
    public IndexIdentity generateIndexIndentity();
    public IndexIdentity getResourceIndexIdentity(String resource);
    public void setResourceIndexIdentity(String resource, IndexIdentity indexIdentity);
}
