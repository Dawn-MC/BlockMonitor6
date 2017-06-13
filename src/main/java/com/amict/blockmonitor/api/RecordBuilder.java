package com.amict.blockmonitor.api;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Created by johnfg10 on 13/06/2017.
 */
public class RecordBuilder extends Thread {
    protected final Event event;

    public RecordBuilder(Event event) {
        this.event = event;
    }

    @Override
    public void run() {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (event instanceof ClientConnectionEvent.Join) {
            ClientConnectionEvent.Join connectionEvent = (ClientConnectionEvent.Join) event;
            Record record = new Record();
            record.setEventType(EventType.ConnectionEvent);
            record.setLocalDateTime(localDateTime);
            record.writeUser(connectionEvent.getTargetEntity());
            record.setLocation(connectionEvent.getTargetEntity().getLocation());
            record.submitToDatabase();
        } else if (event instanceof ClientConnectionEvent.Disconnect) {
            ClientConnectionEvent.Disconnect disconnectEvent = (ClientConnectionEvent.Disconnect) event;
            Record record = new Record();
            record.setEventType(EventType.DisconnectionEvent);
            record.setLocalDateTime(localDateTime);
            record.writeUser(disconnectEvent.getTargetEntity());
            record.setLocation(disconnectEvent.getTargetEntity().getLocation());
            record.submitToDatabase();
        } else if (event instanceof ChangeBlockEvent.Break) {
            ChangeBlockEvent.Break breakEvent = (ChangeBlockEvent.Break) event;
            if (!isPlayerOrEntity(breakEvent.getCause()))
                return;

            List<Transaction<BlockSnapshot>> transactionList = breakEvent.getTransactions();
            for (Transaction<BlockSnapshot> blockSnapshotTransaction : transactionList) {
                Record record = new Record();
                record.setEventType(EventType.BlockBreak);
                record.setLocalDateTime(localDateTime);
                dealWithEntityType(breakEvent.getCause(), record);
                Optional<Location<World>> worldLocation = blockSnapshotTransaction.getOriginal().getLocation();
                if (worldLocation.isPresent())
                    record.setLocation(worldLocation.get());
                record.writeBlockSnapshotTransaction(blockSnapshotTransaction);
                record.submitToDatabase();
            }
        } else if (event instanceof ChangeBlockEvent.Place) {
            ChangeBlockEvent.Place placeEvent = (ChangeBlockEvent.Place) event;
            if (!isPlayerOrEntity(placeEvent.getCause()))
                return;

            List<Transaction<BlockSnapshot>> transactionList = placeEvent.getTransactions();
            for (Transaction<BlockSnapshot> blockSnapshotTransaction : transactionList) {
                Record record = new Record();
                record.setEventType(EventType.BlockPlace);
                record.setLocalDateTime(localDateTime);
                dealWithEntityType(placeEvent.getCause(), record);
                Optional<Location<World>> worldLocation = blockSnapshotTransaction.getOriginal().getLocation();
                if (worldLocation.isPresent())
                    record.setLocation(worldLocation.get());
                record.writeBlockSnapshotTransaction(blockSnapshotTransaction);
                record.submitToDatabase();
            }
        }

    }


    private void dealWithEntityType(Cause cause, Record record) {
        Optional<Player> playerOptional = cause.first(Player.class);
        Optional<Entity> entityOptional = cause.first(Entity.class);

        if (playerOptional.isPresent()) {
            record.writeUser(playerOptional.get());
            return;
        } else if (entityOptional.isPresent()) {
            record.writeEntity(entityOptional.get());
            return;
        }
    }

    private boolean isPlayerOrEntity(Cause cause) {
        return cause.containsType(Player.class) || cause.containsType(Entity.class);
    }
}
