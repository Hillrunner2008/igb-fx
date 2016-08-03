package org.lorainelab.igb.visualization;

import org.lorainelab.igb.visualization.component.api.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@aQute.bnd.annotation.component.Component(immediate = true, provide = MainController.class)
public class MainController {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    private Component app;

    public MainController() {
    }

    private void initialize() {

    }

}
