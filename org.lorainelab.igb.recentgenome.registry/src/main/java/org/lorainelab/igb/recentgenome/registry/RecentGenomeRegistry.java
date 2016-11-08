/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.recentgenome.registry;

import javafx.beans.property.ReadOnlyListWrapper;
import org.lorainelab.igb.data.model.GenomeVersion;

/**
 *
 * @author Devdatta
 */
public interface RecentGenomeRegistry {

    ReadOnlyListWrapper<GenomeVersion> getRecentGenomes();

    void addRecentGenome(GenomeVersion recentGenomeVersion);

    void clearRecentGenomes();
}
