package com.dawnmcdevelop.blockmonitor.listeners;

import com.dawnmcdevelop.blockmonitor.BlockMonitor;
import com.dawnmcdevelop.blockmonitor.api.RecordBuilder;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;

/**
 * Created by johnfg10 on 14/06/2017.
 */
public class onInteractInventoryEvent {
    @Listener
    public void InteractInventoryEvent(InteractInventoryEvent event){
        RecordBuilder recordBuilder = new RecordBuilder(event);
        BlockMonitor.executor.execute(recordBuilder);
    }
}
