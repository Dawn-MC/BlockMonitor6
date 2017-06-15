package com.amict.blockmonitor.listeners;

import com.amict.blockmonitor.BlockMonitor;
import com.amict.blockmonitor.api.RecordBuilder;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;

/**
 * Created by johnfg10 on 15/06/2017.
 */
public class onChangeInventoryEventTransfer {

    @Listener
    public void ChangeInventoryEventTransfer(ChangeInventoryEvent.Transfer event){
        RecordBuilder recordBuilder = new RecordBuilder(event);
        BlockMonitor.executor.execute(recordBuilder);
    }
}
