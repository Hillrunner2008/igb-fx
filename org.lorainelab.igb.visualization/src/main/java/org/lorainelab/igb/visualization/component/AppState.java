/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Sets;
import java.util.Set;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.visualization.component.api.State;
import org.lorainelab.igb.visualization.model.TrackRenderer;

/**
 *
 * @author jeckstei
 */
public class AppState implements State {

        private double totalTrackHeight;
        private Set<TrackRenderer> trackRenderers;
        private double scrollX;
        private double scrollY;
        private double scrollYVisibleAmount;
        private double hSlider;
        private double vSlider;
        private GenomeVersion selectedGenomeVersion;
        private Set<DataSet> loadedDataSets;
        private Chromosome selectedChromosome;
        private static AppState instance;

        public AppState() {
            this.trackRenderers = Sets.newConcurrentHashSet();
            this.loadedDataSets = Sets.newConcurrentHashSet();
        }

        public static AppState factory() {
            if (instance == null) {
                instance = new AppState();
            }
            return instance;
        }

        public Chromosome getSelectedChromosome() {
            return selectedChromosome;
        }

        public AppState setSelectedChromosome(Chromosome selectedChromosome) {
            this.selectedChromosome = selectedChromosome;
            return this;
        }

        public Set<DataSet> getLoadedDataSets() {
            return loadedDataSets;
        }

        public AppState setLoadedDataSets(Set<DataSet> loadedDataSets) {
            this.loadedDataSets.clear();
            this.loadedDataSets.addAll(loadedDataSets);
            return this;
        }

        public GenomeVersion getSelectedGenomeVersion() {
            return selectedGenomeVersion;
        }

        public AppState setSelectedGenomeVersion(GenomeVersion selectedGenomeVersion) {
            this.selectedGenomeVersion = selectedGenomeVersion;
            return this;
        }

        public double getScrollX() {
            return scrollX;
        }

        public AppState setScrollX(double scrollX) {
            this.scrollX = scrollX;
            return this;
        }

        public double getScrollY() {
            return scrollY;
        }

        public AppState setScrollY(double scrollY) {
            this.scrollY = scrollY;
            return this;
        }

        public double getScrollYVisibleAmount() {
            return scrollYVisibleAmount;
        }

        public AppState setScrollYVisibleAmount(double scrollYVisibleAmount) {
            this.scrollYVisibleAmount = scrollYVisibleAmount;
            return this;
        }

        public double gethSlider() {
            return hSlider;
        }

        public AppState sethSlider(double hSlider) {
            this.hSlider = hSlider;
            return this;
        }

        public double getvSlider() {
            return vSlider;
        }

        public AppState setvSlider(double vSlider) {
            this.vSlider = vSlider;
            return this;
        }

        public double getTotalTrackHeight() {
            return totalTrackHeight;
        }

        public AppState setTotalTrackHeight(double totalTrackHeight) {
            this.totalTrackHeight = totalTrackHeight;
            return this;
        }

        public Set<TrackRenderer> getTrackRenderers() {
            return trackRenderers;
        }

        public AppState setTrackRenderers(Set<TrackRenderer> trackRenderers) {
            this.trackRenderers.clear();
            this.trackRenderers.addAll(trackRenderers);
            return this;
        }

    }