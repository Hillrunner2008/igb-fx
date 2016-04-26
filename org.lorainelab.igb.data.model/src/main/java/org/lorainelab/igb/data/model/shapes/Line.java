/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.shapes;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.lorainelab.igb.data.model.Feature;

/**
 *
 * @author dcnorris
 */
public class Line<T extends Feature> implements Shape {

    private Feature model;
    private final List<Attribute> attributes;
    private int offset;
    private int width;

    public Line() {
        this.attributes = Lists.newArrayList();
    }

    public static Build start(int offset, int width) {
        return new Builder(offset, width);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getOffset() {
        return this.offset;
    }

    @Override
    public Optional<Feature> getModel() {
        return Optional.ofNullable(this.model);
    }

    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    public enum Attribute {

        dotted;
    }

    public interface Build {

        Build linkToModel(Feature model);

        Build addAttribute(Line.Attribute attr);

        Line build();
    }

    private static class Builder implements Build {

        Line instance = new Line();

        public Builder(int offset, int width) {
            instance.offset = offset;
            instance.width = width;
        }

        @Override
        public Build linkToModel(Feature model) {
            instance.model = model;
            return this;
        }

        @Override
        public Build addAttribute(Attribute attr) {
            instance.attributes.add(attr);
            return this;
        }

        @Override
        public Line build() {
            return instance;
        }

    }
}
