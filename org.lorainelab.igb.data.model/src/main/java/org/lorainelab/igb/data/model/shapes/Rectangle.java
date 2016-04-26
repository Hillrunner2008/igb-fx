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
 * @author jeckstei
 * @param <T>
 */
public class Rectangle<T extends Feature> implements Shape {

    private Feature model;
    private String innerText;
    private final List<Attribute> attributes;
    private int offset;
    private int width;

    public Rectangle() {
        attributes = Lists.newArrayList();
        innerText = "";
    }

    public static Build start(int offset, int width) {
        return new Rectangle.Builder(offset, width);
    }

    public static Build start(int offset, int width, Feature model) {
        return new Rectangle.Builder(offset, width);
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

    public String getInnerText() {
        return this.innerText;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    public enum Attribute {

        thick,
        insertion,
        deletion
    }

    public interface Build {

        public Build setInnerText(String innerText);

        Build linkToModel(Feature model);

        Build addAttribute(Attribute attr);

        Rectangle build();
    }

    private static class Builder implements Build {

        Rectangle instance = new Rectangle();

        public Builder(int offset, int width) {
            instance.offset = offset;
            instance.width = width;
        }

        @Override
        public Build setInnerText(String innerText) {
            instance.innerText = innerText;
            return this;
        }

        @Override
        public Build addAttribute(Attribute attr) {
            instance.attributes.add(attr);
            return this;
        }

        @Override
        public Rectangle build() {
            return instance;
        }

        @Override
        public Build linkToModel(Feature model) {
            instance.model = model;
            return this;
        }

    }
}
