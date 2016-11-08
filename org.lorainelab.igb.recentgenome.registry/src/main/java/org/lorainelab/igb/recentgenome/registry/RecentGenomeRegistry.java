/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.recentgenome.registry;

import javafx.beans.property.ReadOnlyListWrapper;

/**
 *
 * @author Devdatta
 */
public interface RecentGenomeRegistry {

    ReadOnlyListWrapper<String> getRecentGenomes();

    void addRecentGenome(String recentFile);

    void clearRecentGenomes();
}
