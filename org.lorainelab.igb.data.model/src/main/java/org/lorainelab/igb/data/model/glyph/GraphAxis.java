package org.lorainelab.igb.data.model.glyph;

import static org.lorainelab.igb.data.model.glyph.AxisUtil.getMajorTick;

/**
 *
 * @author dcnorris
 */
public class GraphAxis extends UnlockedNumberAxis {

    public GraphAxis() {
        super();
    }
    
    public GraphAxis(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
        setTickUnit(getMajorTick(upperBound - lowerBound));

    }

    @Override
    public void setPrefSize(double prefWidth, double prefHeight) {
        super.setPrefSize(prefWidth, prefHeight); 
        setWidth(prefWidth);
        setHeight(prefHeight);
    }
    

    public final void updateUpperBound(double value) {
        super.setUpperBound(value);
        setTickUnit(getMajorTick(getUpperBound() - getLowerBound()));
    }
    public final void updateLowerBound(double value) {
        super.setLowerBound(value);
        setTickUnit(getMajorTick(getUpperBound() - getLowerBound()));
    }

    

}
