package com.dawnmcdevelop.blockmonitor.listeners;

import com.dawnmcdevelop.blockmonitor.BlockMonitor;
import com.dawnmcdevelop.blockmonitor.api.Record;
import com.dawnmcdevelop.blockmonitor.api.RecordBuilder;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.EventListener;
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
