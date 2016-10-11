package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import org.lorainelab.igb.view.api.ViewService;
import org.lorainelab.igb.visualization.model.CanvasModel;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class ViewServiceImpl implements ViewService {

    private CanvasModel canvasModel;

    @Override
    public Runnable getRefreshViewAction() {
        return canvasModel.getRefreshAction();
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

}
