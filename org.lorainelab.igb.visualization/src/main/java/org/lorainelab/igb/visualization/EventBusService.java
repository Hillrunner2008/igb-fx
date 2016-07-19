package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Component;
import com.google.common.eventbus.EventBus;

@Component(immediate = true, provide = EventBusService.class)
public class EventBusService {

    private static EventBus bus;

    public EventBusService() {
    }


}
