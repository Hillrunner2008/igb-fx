/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.datasetloadingservice;

import java.io.File;

/**
 *
 * @author Devdatta Kulkarni
 */
public interface OpenDataSet {
    void openFile();
    void openFile(File file);
}