package com.amict.blockmonitor.listeners;

import com.amict.blockmonitor.api.Record;
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
        if (event instanceof ChangeBlockEvent.Break) {
            Record record = new Record();
            record.setEvent(event);
        }else if (event instanceof ChangeBlockEvent.Place){

        }
    }
}
