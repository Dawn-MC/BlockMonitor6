package com.amict.blockmonitor.api;

import com.amict.blockmonitor.BlockMonitor;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Created by johnfg10 on 04/06/2017.
 */
public class Record {
    DataContainer dataContainer;
    EventType eventType;
    Location<World> location;
    LocalDateTime localDateTime;

    @Inject
    private Logger logger;


    public Record(){
        this.dataContainer = DataContainer.createNew();
        this.eventType = EventType.Unknown;
        this.localDateTime = LocalDateTime.now();
    }

    @Deprecated
    public void setEvent(Event event){
        Cause cause = event.getCause();

        if (event instanceof ClientConnectionEvent.Join) {
            //we know its a client connect join event
            ClientConnectionEvent.Join clientConnectionEventJoin = (ClientConnectionEvent.Join) event;
            this.eventType = EventType.ConnectionEvent;
            setEntity(clientConnectionEventJoin.getTargetEntity(), true);
            this.submitToDatabase();
        }
        if (event instanceof ClientConnectionEvent.Disconnect){
            //we know its a client connect disconnect event
            ClientConnectionEvent.Disconnect clientConnectionEventDisconnect = (ClientConnectionEvent.Disconnect) event;
            this.eventType = EventType.DisconnectionEvent;
            this.location = clientConnectionEventDisconnect.getTargetEntity().getLocation();
            setEntity(clientConnectionEventDisconnect.getTargetEntity(), true);
            this.submitToDatabase();
        }

        if (event instanceof ChangeBlockEvent.Break){
            ChangeBlockEvent.Break changeBlockEventBreak = (ChangeBlockEvent.Break) event;
            List<Transaction<BlockSnapshot>> blockSnapshotTransaction = changeBlockEventBreak.getTransactions();
            for (Transaction<BlockSnapshot> transaction : blockSnapshotTransaction) {
                if (!transaction.getOriginal().getState().getType().getName().equalsIgnoreCase("minecraft:air")){
                    clearBlockTransactionData();
                    clearEntityData();
                    Optional<Player> playerOptional = event.getCause().first(Player.class);
                    if (playerOptional.isPresent()){
                        System.out.println("player present");
                        this.eventType = EventType.BlockBreak;
                        Player player = playerOptional.get();
                        setEntity(player, false);
                        writeBlockTransaction(transaction);

                        this.submitToDatabase();
                    }
                }
            }
        }

        if (event instanceof ChangeBlockEvent.Place){
            ChangeBlockEvent.Place changeBlockEventPlace = (ChangeBlockEvent.Place) event;
        }
    }
    @Deprecated
    public void setEntity(Entity entity, boolean setLocation){
        if (entity instanceof Player){
            Player player = (Player) entity;
            User user = player;
            this.dataContainer.set(DataQuery.of("user"), user.toContainer());

            if (setLocation){
                location = player.getLocation();
            }
        }else if (entity instanceof Living){

        }else if (entity instanceof Explosion){

        }
    }
    @Deprecated
    public void clearEntityData(){
        this.dataContainer.remove(DataQuery.of("user"));
    }
    @Deprecated
    public void writeBlockTransaction(Transaction<BlockSnapshot> blockSnapshotTransaction){
        BlockSnapshot originalBlockSnapshot = blockSnapshotTransaction.getOriginal();
        if (!originalBlockSnapshot.getState().getType().getName().equalsIgnoreCase("minecraft:air")) {
            Optional<Location<World>> worldLocation = originalBlockSnapshot.getLocation();

            if (worldLocation.isPresent())
                this.location = worldLocation.get();

            this.dataContainer.set(DataQuery.of("block"), originalBlockSnapshot.toContainer());
        }
    }
    @Deprecated
    public void clearBlockTransactionData(){
        this.location = null;
        this.dataContainer.remove(DataQuery.of("block"));
    }


    public void writeEntity(Entity entity){
        this.dataContainer.set(DataQuery.of("Entity"), entity.toContainer());
    }

    public void writeUser(User user){
        this.dataContainer.set(DataQuery.of("User"), user.toContainer());
    }

    public void writeItemSnapshotTransaction(Transaction<ItemStackSnapshot> itemStackSnapshotTransaction){
        this.dataContainer.set(DataQuery.of("ItemOriginal"), itemStackSnapshotTransaction.getOriginal().toContainer());
        this.dataContainer.set(DataQuery.of("ItemFinal"), itemStackSnapshotTransaction.getFinal().toContainer());
    }

    public void writeBlockSnapshotTransaction(Transaction<BlockSnapshot> blockSnapshotTransaction){
        this.dataContainer.set(DataQuery.of("BlockOriginal"), blockSnapshotTransaction.getOriginal().toContainer());
        this.dataContainer.set(DataQuery.of("BlockFinal"), blockSnapshotTransaction.getFinal().toContainer());
    }

    public void setLocalDateTime(LocalDateTime localDateTime1){
        this.localDateTime = localDateTime1;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void setLocation(Location<World> location) {
        this.location = location;
    }

    public void submitToDatabase(){
        String prepareStatementString = "INSERT INTO `blockmonitor` (`locationX`,`locationY`,`locationZ`,`worldName`,`eventtype`,`datacontainer`, `timestamp`) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try {
            DataFormat dataFormat = DataFormats.JSON;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                dataFormat.writeTo(outputStream, dataContainer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String output = new String(outputStream.toByteArray(), Charset.defaultCharset());
            Connection connection = BlockMonitor.storageHandler.dataSource.getConnection();

            PreparedStatement prepareStatement = connection.prepareStatement(prepareStatementString);

            if (location != null){
                prepareStatement.setInt(1, location.getBlockX());
                prepareStatement.setInt(2, location.getBlockY());
                prepareStatement.setInt(3, location.getBlockZ());
                prepareStatement.setString(4, location.getExtent().getName());
            }else{
                prepareStatement.setDouble(1, 0);
                prepareStatement.setDouble(2, 0);
                prepareStatement.setDouble(3, 0);
                prepareStatement.setString(4, "unknown");
            }
            prepareStatement.setString(5, eventType.name());
            prepareStatement.setString(6, output);
            prepareStatement.setTimestamp(7, Timestamp.valueOf(localDateTime));
            prepareStatement.execute();
            //cleanup
            prepareStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
