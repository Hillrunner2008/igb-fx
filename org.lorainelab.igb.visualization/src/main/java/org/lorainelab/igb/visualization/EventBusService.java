package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Component;
import com.google.common.eventbus.EventBus;

@Component(immediate = true, provide = EventBusService.class)
public class EventBusService {

    private static final EventBus bus = new EventBus();

    public EventBusService() {
    }

    public EventBus getEventBus() {
        return bus;
    }

    public static EventBus getModuleEventBus() {
        return bus;
    }
}
