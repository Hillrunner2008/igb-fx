package org.lorainelab.igb.filehandler.bigwig;

import org.lorainelab.igb.data.model.shapes.Composition;
import org.lorainelab.igb.data.model.shapes.Rectangle;
import org.lorainelab.igb.data.model.view.Renderer;

/**
 *
 * @author dcnorris
 */
public class BigWigRenderer implements Renderer<BigWigFeature>{

    @Override
    public Composition render(BigWigFeature feature) {
          return composition(
                null,
                null,
                layer(
                        0,
                        Rectangle.start(feature.getRange().lowerEndpoint(), feature.getRange().upperEndpoint()-feature.getRange().lowerEndpoint()).build()
                )
        );
    }

}
