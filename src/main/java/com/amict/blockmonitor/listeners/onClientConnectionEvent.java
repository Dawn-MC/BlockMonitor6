package com.amict.blockmonitor.listeners;

import com.amict.blockmonitor.api.Record;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by johnfg10 on 04/06/2017.
 */
public class onClientConnectionEvent {

    @Listener(order = Order.LAST)
    public void clientConnectionEvent(ClientConnectionEvent event){
        if (event instanceof ClientConnectionEvent.Join) {
            Record record = new Record();
            record.setCause(event);
        }
        //record.submitToDatabase();
    }
}
