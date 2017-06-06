package com.amict.blockmonitor.api;

import com.amict.blockmonitor.BlockMonitor;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by johnfg10 on 04/06/2017.
 */
public class Record {
    DataContainer dataContainer;
    EventType eventType;
    Location<World> worldLocation;
    String worldName;

    Record(DataContainer dataContainer1, EventType eventType1, Location<World> loc, String string){
        this.dataContainer = dataContainer1;
        this.eventType = eventType1;
        this.worldLocation = loc;
        this.worldName = string;
    }

    public Record(){
        this.dataContainer = DataContainer.createNew();
    }

    public void setCause(Cause cause){
        Optional<Player> playerOptional = cause.first(Player.class);
        if (playerOptional.isPresent()){
            Player player = playerOptional.get();

            this.worldName = player.getWorld().getName();
            this.worldLocation = player.getLocation();
            this.dataContainer.set(DataQuery.of("type"), "player");
            this.dataContainer.set(DataQuery.of("player", "uuid"), player.getUniqueId());
            this.dataContainer.set(DataQuery.of("player", "username"), player.getName());
            this.dataContainer.set(DataQuery.of("player", "ipaddress"), player.getConnection().getAddress().toString());
        }

        Optional<Entity> entityOptional = cause.first(Entity.class);
        if (entityOptional.isPresent()){
            Entity entity = entityOptional.get();

            this.worldName = entity.getWorld().getName();
            this.worldLocation = entity.getLocation();
            this.dataContainer.set(DataQuery.of("type"), "entity");
            this.dataContainer.set(DataQuery.of("entity", "uuid"), entity.getUniqueId());
            this.dataContainer.set(DataQuery.of("entity", "type"), entity.getType());
            if (entity.getCreator().isPresent())
            this.dataContainer.set(DataQuery.of("entity", "creator"), entity.getCreator().get());
        }
    }

    public void joinEvent(LocalDateTime dateTime){
        eventType = EventType.ConnectionEvent;
        this.dataContainer.set(DataQuery.of("time"),dateTime);
    }

    public void leaveEvent(LocalDateTime dateTime){
        eventType = EventType.DisconnectionEvent;
        this.dataContainer.set(DataQuery.of("time"), dateTime);
    }

    public void blockRemovedEvent(Transaction<BlockSnapshot> blockSnapshotTransaction){
        eventType = EventType.blockRemoved;
        this.dataContainer.set(DataQuery.of("block", "blocksnapshot"), blockSnapshotTransaction.getFinal());
    }

    //public DataContainer writeBlockSnapshot

    public void submitToDatabase(){
        String prepareStatementString = "INSERT INTO `blockmonitor` (`locationX`,`locationY`,`locationZ`,`worldName`,`eventtype`,`datacontiner` ) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            DataFormat dataFormat = DataFormats.JSON;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            dataFormat.writeTo(outputStream, dataContainer);
            String dataContainerAsJson = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

            Connection connection = BlockMonitor.storageHandler.dataSource.getConnection();
            PreparedStatement prepareStatement = connection.prepareStatement(prepareStatementString);

            prepareStatement.setInt(1, this.worldLocation.getBlockX());
            prepareStatement.setInt(2, this.worldLocation.getBlockY());
            prepareStatement.setInt(3, this.worldLocation.getBlockZ());
            prepareStatement.setString(4, worldName);
            prepareStatement.setString(5, eventType.toString());
            prepareStatement.setString(6, dataContainerAsJson);
            System.out.println("did excecute correctly: " + prepareStatement.execute());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
