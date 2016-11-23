package org.lorainelab.igb.selections;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class ListenerTest {

    private static final Logger LOG = LoggerFactory.getLogger(ListenerTest.class);
    private SelectionInfoService selectionInfoService;

    @Activate
    public void activate() {
        changeListener = new ChangeListener<Optional<GenomeVersion>>() {
            @Override
            public void changed(ObservableValue<? extends Optional<GenomeVersion>> observable, Optional<GenomeVersion> oldValue, Optional<GenomeVersion> newValue) {
                LOG.info("selectedGenomeVersion change detected");
            }
        };
        selectionInfoService.getSelectedGenomeVersion().addListener(new WeakChangeListener<>(changeListener));
    }
    private ChangeListener<Optional<GenomeVersion>> changeListener;

    @Reference(optional = false)
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }
}
