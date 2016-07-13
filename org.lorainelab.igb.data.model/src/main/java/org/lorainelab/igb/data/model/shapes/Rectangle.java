/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.shapes;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javafx.scene.paint.Color;
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
    // a function that can be defined to translate (or mirror) reference sequence as the innerText of this shape
    // the function should expect as input the reference sequence overlapping this shape, or if desired a custom range can be defined using the
    // innerTextReferenceSequenceRange variable
    private Function<String, String> innerTextRefSeqTranslator;
    //enables a larger reference sequence context when defining a function for translation
    private Range<Integer> innerTextReferenceSequenceRange;
    private Color color;
    private boolean colorByBase;

    public Rectangle() {
        attributes = Lists.newArrayList();
        innerText = "";
    }

    public static Build start(int offset, int width) {
        return new Rectangle.Builder(offset, width);
    }

    public static Build start(int offset, int width, String innerText) {
        Builder builder = new Rectangle.Builder(offset, width);
        builder.setInnerText(innerText);
        return builder;
    }

    public static Build start(int offset, int width, Feature model) {
        Builder builder = new Rectangle.Builder(offset, width);
        builder.linkToModel(model);
        return builder;
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

    public Optional<Function<String, String>> getInnerTextRefSeqTranslator() {
        return Optional.ofNullable(innerTextRefSeqTranslator);
    }

    public Optional<Range<Integer>> getInnerTextReferenceSequenceRange() {
        return Optional.ofNullable(innerTextReferenceSequenceRange);
    }

    public Optional<Color> getColor() {
        return Optional.ofNullable(color);
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean getColorByBase() {
        return colorByBase;
    }

    public enum Attribute {

        THICK,
        INSERTION
    }

    public interface Build {

        Build setInnerText(String innerText);

        Build setInnerTextRefSeqTranslator(Function<String, String> innerTextRefSeqTranslator);

        Build setInnerTextReferenceSequenceRange(Range<Integer> innerTextReferenceSequenceRange);

        Build setColor(Color color);

        Build setColorByBase(boolean colorByBase);

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
        public Build setInnerTextRefSeqTranslator(Function<String, String> innerTextRefSeqTranslator) {
            instance.innerTextRefSeqTranslator = innerTextRefSeqTranslator;
            return this;
        }

        @Override
        public Build setInnerTextReferenceSequenceRange(Range<Integer> innerTextReferenceSequenceRange) {
            instance.innerTextReferenceSequenceRange = innerTextReferenceSequenceRange;
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

        @Override
        public Build setColor(Color color) {
            instance.color = color;
            return this;
        }

        @Override
        public Build setColorByBase(boolean colorByBase) {
            instance.colorByBase = colorByBase;
            return this;
        }

    }
}
