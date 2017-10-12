package com.dawndevelop.blockmonitor.listeners;

import com.dawndevelop.blockmonitor.api.RecordBuilder;
import com.dawndevelop.blockmonitor.BlockMonitor;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;

/**
 * Created by johnfg10 on 19/06/2017.
 */
public class onUseItemStackEvent {

    public void UseItemStackEvent(UseItemStackEvent event){
        RecordBuilder recordBuilder = new RecordBuilder(event);
        BlockMonitor.executor.execute(recordBuilder);
    }
}
