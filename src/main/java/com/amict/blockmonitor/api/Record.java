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
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
                    System.out.println("found a block");
                    this.eventType = EventType.BlockBreak;
                    Optional<Player> playerOptional = event.getCause().first(Player.class);
                    if (playerOptional.isPresent()){
                        Player player = playerOptional.get();
                        setEntity(player, false);
                    }

                    writeBlockTransaction(transaction);

                    this.submitToDatabase();
                }
            }
        }

        if (event instanceof ChangeBlockEvent.Place){
            ChangeBlockEvent.Place changeBlockEventPlace = (ChangeBlockEvent.Place) event;
        }
    }

    public void setEntity(Entity entity, boolean setLocation){
        if (entity instanceof Player){
            Player player = (Player) entity;

            this.dataContainer.set(DataQuery.of("player"), player.toContainer());
            if (setLocation){
                location = player.getLocation();
            }
        }else if (entity instanceof Living){

        }else if (entity instanceof Explosion){

        }
    }

    public void writeBlockTransaction(Transaction<BlockSnapshot> blockSnapshotTransaction){
        if (!blockSnapshotTransaction.getOriginal().getState().getType().getName().equalsIgnoreCase("minecraft:air")) {

            Optional<Location<World>> worldLocation = blockSnapshotTransaction.getFinal().getLocation();
            if (worldLocation.isPresent())
                this.location = worldLocation.get();

            this.dataContainer.set(DataQuery.of("block", "blockcontainer"), blockSnapshotTransaction.getOriginal().getState().toContainer());
        }

    }

    public void clearBlockTransactionData(){
        this.location = null;
        this.dataContainer.remove(DataQuery.of("block", "blockcontainer"));
    }

    public Optional<Location> getLocationFromCause(Cause cause){
        Optional<Location> locationOptional = cause.first(Location.class);
        if (locationOptional.isPresent()){
            Location location = locationOptional.get();
            return Optional.of(location);
        }else {
            logger.info("world location could not be found");
        }
        return Optional.empty();

    }

    //public DataContainer writeBlockSnapshot

    public void submitToDatabase(){
        String prepareStatementString = "INSERT INTO `blockmonitor` (`locationX`,`locationY`,`locationZ`,`worldName`,`eventtype`,`datacontiner`, `timestamp`) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try {
            DataFormat dataFormat = DataFormats.JSON;
            OutputStream outputStream = new ByteArrayOutputStream();
            try {
                dataFormat.writeTo(outputStream, dataContainer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            outputStream.write(byteArrayOutputStream.toByteArray());
            String outputString = byteArrayOutputStream.toString();

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
            prepareStatement.setString(6, outputString);
            prepareStatement.setTimestamp(7, Timestamp.valueOf(localDateTime));
            System.out.println("Record submit to db : " + prepareStatement.execute());

            //cleanup
            prepareStatement.close();
            connection.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
