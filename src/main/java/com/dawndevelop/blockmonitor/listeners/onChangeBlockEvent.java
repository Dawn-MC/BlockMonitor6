package com.dawndevelop.blockmonitor.listeners;

import com.dawndevelop.blockmonitor.api.RecordBuilder;
import com.dawndevelop.blockmonitor.BlockMonitor;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

/**
 * Created by johnfg10 on 06/06/2017.
 */
public class onChangeBlockEvent{
    @Listener
    public void onChangeBlockEvent(ChangeBlockEvent event){
        RecordBuilder recordBuilder = new RecordBuilder(event);
        BlockMonitor.executor.execute(recordBuilder);
    }
}
