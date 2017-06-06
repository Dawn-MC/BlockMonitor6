package com.amict.blockmonitor.listeners;

import com.amict.blockmonitor.api.Record;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by johnfg10 on 04/06/2017.
 */
public class onClientConnectionEvent implements EventListener<ClientConnectionEvent> {
    @Override
    public void handle(ClientConnectionEvent event) throws Exception {
        if (event instanceof ClientConnectionEvent.Join){
            Optional<Player> player = event.getCause().first(Player.class);
            if (player.isPresent()){
                Record record = new Record();
                record.setCause(event.getCause());
                record.joinEvent(LocalDateTime.now());
                record.submitToDatabase();
            }
        }else if (event instanceof ClientConnectionEvent.Disconnect){
            Optional<Player> player = event.getCause().first(Player.class);
            if (player.isPresent()){
                Record record = new Record();
                record.setCause(event.getCause());
                record.leaveEvent(LocalDateTime.now());
                record.submitToDatabase();
            }
        }
    }
}
