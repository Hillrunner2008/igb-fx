package org.lorainelab.igb.data.model;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.junit.Test;

/**
 *
 * @author dcnorris
 */
public class AffineTest {

    @Test
    public void scale() throws NonInvertibleTransformException {
        Scale scale = new Scale(2, 2);
        Point2D p = new Point2D(2, 2);
        Point2D scaledUp = scale.transform(p);
        System.out.println(scaledUp);
        System.out.println(scale.inverseTransform(scaledUp));
    }

    @Test
    public void translate() {
        Translate t = new Translate(100, 100);
        Point2D p = new Point2D(2, 2);
        System.out.println(t.transform(p));
    }

    @Test
    public void calculateTranslation() {
        Rectangle2D scene = new Rectangle2D(0, 0, 1000, 1000);
        Rectangle2D model = new Rectangle2D(0, 0, 10000, 10000);
        Rectangle2D shapeInModel = new Rectangle2D(0, 0, 50, 50);
        Scale scale = new Scale(scene.getWidth() / model.getWidth(), scene.getHeight() / model.getHeight());
        Point2D transformedShape = scale.transform(new Point2D(shapeInModel.getWidth(), shapeInModel.getHeight()));
        System.out.println(transformedShape);
    }

}
