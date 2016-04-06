package org.lorainelab.igb.data.model.shapes;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.lorainelab.igb.data.model.view.Layer;

/**
 *
 * @author dcnorris
 */
public interface Composition {

    default Composition composition(String label, Map<String, String> tooltipData, Layer... layers) {
        return new Composition() {
            List<Layer> layersList = Arrays.asList(layers);

            @Override
            public Optional<String> getLabel() {
                return Optional.ofNullable(label);
            }

            @Override
            public List<Layer> getLayers() {
                return layersList;
            }

            @Override
            public Map<String, String> getTooltipData() {
                return tooltipData;
            }

        };
    }

    default Optional<String> getLabel() {
        return Optional.empty();
    }

    default List<Layer> getLayers() {
        return Collections.EMPTY_LIST;
    }

    default Map<String, String> getTooltipData() {
        return Maps.newConcurrentMap();
    }
}
