package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import org.lorainelab.igb.data.model.action.AbstractIgbAction;
import org.lorainelab.igb.data.model.action.IgbAction;
import org.lorainelab.igb.view.api.ViewService;
import org.lorainelab.igb.visualization.model.CanvasModel;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class ViewServiceImpl extends AbstractIgbAction implements ViewService, IgbAction {

    private CanvasModel canvasModel;

    public ViewServiceImpl() {
        setText("Refresh Main View");
        setGraphic(new FontAwesomeIconView(FontAwesomeIcon.REFRESH));
    }

    @Override
    public Runnable getRefreshViewAction() {
        return canvasModel.getRefreshAction();
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

}
