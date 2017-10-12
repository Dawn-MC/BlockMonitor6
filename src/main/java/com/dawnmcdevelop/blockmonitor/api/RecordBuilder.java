package com.dawnmcdevelop.blockmonitor.api;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Created by johnfg10 on 13/06/2017.
 */
public class RecordBuilder implements Runnable {

    protected Event event;
    LocalDateTime localDateTime = LocalDateTime.now();
    public RecordBuilder(Event event1) {
        this.event = event1;
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

    private void dealWithEntityTypeWithLocation(Cause cause, Record record) {
        Optional<Player> playerOptional = cause.first(Player.class);
        Optional<Entity> entityOptional = cause.first(Entity.class);

        if (playerOptional.isPresent()) {
            record.writeUser(playerOptional.get());
            record.setLocation(playerOptional.get().getLocation());
            return;
        } else if (entityOptional.isPresent()) {
            record.writeEntity(entityOptional.get());
            record.setLocation(entityOptional.get().getLocation());
            return;
        }
    }

    @Override
    public void run() {
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
        } else if (event instanceof ChangeBlockEvent.Modify){
            ChangeBlockEvent.Modify modifyEvent = (ChangeBlockEvent.Modify) event;

            List<Transaction<BlockSnapshot>> transactionList = modifyEvent.getTransactions();
            for (Transaction<BlockSnapshot> blockSnapshotTransaction : transactionList) {
                Record record = new Record();
                record.setEventType(EventType.BlockModify);
                record.setLocalDateTime(localDateTime);
                dealWithEntityType(modifyEvent.getCause(), record);
                Optional<Location<World>> worldLocation = blockSnapshotTransaction.getOriginal().getLocation();
                if (worldLocation.isPresent())
                    record.setLocation(worldLocation.get());
                record.writeBlockSnapshotTransaction(blockSnapshotTransaction);
                record.submitToDatabase();
            }
        }else if (event instanceof ChangeBlockEvent.Grow){
            ChangeBlockEvent.Grow growEvent = (ChangeBlockEvent.Grow) event;
            List<Transaction<BlockSnapshot>> transactionList = growEvent.getTransactions();
            for (Transaction<BlockSnapshot> blockSnapshotTransaction : transactionList) {
                Record record = new Record();
                record.setEventType(EventType.BlockGrow);
                record.setLocalDateTime(localDateTime);
                dealWithEntityType(growEvent.getCause(), record);
                Optional<Location<World>> worldLocation = blockSnapshotTransaction.getOriginal().getLocation();
                if (worldLocation.isPresent())
                    record.setLocation(worldLocation.get());
                record.writeBlockSnapshotTransaction(blockSnapshotTransaction);
                record.submitToDatabase();
            }
        }else if (event instanceof ChangeBlockEvent.Decay){
            ChangeBlockEvent.Decay decayEvent = (ChangeBlockEvent.Decay) event;
            List<Transaction<BlockSnapshot>> transactionList = decayEvent.getTransactions();
            for (Transaction<BlockSnapshot> blockSnapshotTransaction : transactionList) {
                Record record = new Record();
                record.setEventType(EventType.BlockGrow);
                record.setLocalDateTime(localDateTime);
                dealWithEntityType(decayEvent.getCause(), record);
                Optional<Location<World>> worldLocation = blockSnapshotTransaction.getOriginal().getLocation();
                if (worldLocation.isPresent())
                    record.setLocation(worldLocation.get());
                record.writeBlockSnapshotTransaction(blockSnapshotTransaction);
                record.submitToDatabase();
            }
        }else if (event instanceof InteractInventoryEvent){
            InteractInventoryEvent changeInventoryEventTransfer = (InteractInventoryEvent) event;
            Transaction<ItemStackSnapshot> itemStackSnapshotTransaction = changeInventoryEventTransfer.getCursorTransaction();
                Record record = new Record();
                record.setEventType(EventType.InteractInventoryEvent);
                record.setLocalDateTime(localDateTime);
                dealWithEntityTypeWithLocation(changeInventoryEventTransfer.getCause(), record);

                record.writeItemSnapshotTransaction(itemStackSnapshotTransaction);
                record.submitToDatabase();
        }else if(event instanceof UseItemStackEvent.Start){
            UseItemStackEvent.Start useItemStackEventStart = (UseItemStackEvent.Start) event;
            Record record = new Record();
            record.setEventType(EventType.UseItemStackEventStart);
            record.setLocalDateTime(localDateTime);
            dealWithEntityTypeWithLocation(useItemStackEventStart.getCause(), record);

            record.writeItemSnapshot(useItemStackEventStart.getItemStackInUse());
            record.submitToDatabase();
        }else if (event instanceof UseItemStackEvent.Stop){
            UseItemStackEvent.Stop useItemStackEventStop = (UseItemStackEvent.Stop) event;
            Record record = new Record();
            record.setEventType(EventType.UseItemStackEventStart);
            record.setLocalDateTime(localDateTime);
            dealWithEntityTypeWithLocation(useItemStackEventStop.getCause(), record);

            record.writeItemSnapshot(useItemStackEventStop.getItemStackInUse());
            record.submitToDatabase();
        }
    }
}
