package com.amict.blockmonitor.listeners;

import com.amict.blockmonitor.api.Record;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

/**
 * Created by johnfg10 on 06/06/2017.
 */
public class onChangeBlockEvent implements EventListener<ChangeBlockEvent> {
    @Override
    public void handle(ChangeBlockEvent event) throws Exception {

        if (event instanceof ChangeBlockEvent.Break){
            for (Transaction<BlockSnapshot> blockSnapshotTransaction:event.getTransactions()) {
                Record record = new Record();
                record.setCause(event.getCause());
                record.blockRemovedEvent(blockSnapshotTransaction);
            }

        }else if (event instanceof ChangeBlockEvent.Place){

        }else if (event instanceof ChangeBlockEvent.Decay){

        }else if (event instanceof ChangeBlockEvent.Grow){

        }else if (event instanceof ChangeBlockEvent.Modify){

        }
    }
}
