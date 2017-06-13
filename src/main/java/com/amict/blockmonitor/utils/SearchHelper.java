package com.amict.blockmonitor.utils;

import com.amict.blockmonitor.BlockMonitor;
import com.amict.blockmonitor.api.DataContainerHelper;
import com.amict.blockmonitor.api.EventType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.swing.text.html.Option;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.spongepowered.api.text.TextTemplate.*;

/**
 * Created by johnfg10 on 08/06/2017.
 */
public class SearchHelper {
    public static List<Text> searchArea(Location<World> worldLocation, int searchDiameter, Locale locale) throws SQLException {

        String sql = "SELECT * FROM `blockmonitor` WHERE (`worldName` = ?) AND (`locationX` <= ?) AND (`locationX` >= ?) AND (`locationZ` <= ?) AND (`locationZ` >= ?);";
        Connection connection = BlockMonitor.storageHandler.dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, worldLocation.getExtent().getName());
        preparedStatement.setInt(2, worldLocation.getBlockX() + searchDiameter);
        preparedStatement.setInt(3, worldLocation.getBlockX() - searchDiameter);
        preparedStatement.setInt(4, worldLocation.getBlockZ() + searchDiameter);
        preparedStatement.setDouble(5, worldLocation.getBlockZ() - searchDiameter);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<org.spongepowered.api.text.Text> contents = new ArrayList<>();

        while (resultSet.next()) {
            String info = resultSet.getString("datacontainer");
            try {
                InputStream inputStream = new ByteArrayInputStream(info.getBytes());
                DataFormat dataFormat = DataFormats.JSON;
                DataContainer dataContainer = dataFormat.readFrom(inputStream);
                Text text = formattedText(
                        resultSet.getInt("id"),
                        resultSet.getInt("locationX"),
                        resultSet.getInt("locationY"),
                        resultSet.getInt("locationZ"),
                        resultSet.getString("worldName"),
                        resultSet.getString("eventtype"),
                        dataContainer,
                        resultSet.getTimestamp("timestamp").toLocalDateTime(),
                        locale
                );
                contents.add(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        preparedStatement.close();
        connection.close();
        return contents;
    }

    public static List<Text> searchWorld(Location<World> worldLocation, int searchDiameter, Locale locale) throws SQLException {

        String sql = "SELECT * FROM `blockmonitor` WHERE (`worldName` = ?);";
        Connection connection = BlockMonitor.storageHandler.dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, worldLocation.getExtent().getName());

        ResultSet resultSet = preparedStatement.executeQuery();
        List<org.spongepowered.api.text.Text> contents = new ArrayList<>();

        while (resultSet.next()) {
            String info = resultSet.getString("datacontainer");
            try {
                InputStream inputStream = new ByteArrayInputStream(info.getBytes());
                DataFormat dataFormat = DataFormats.JSON;
                DataContainer dataContainer = dataFormat.readFrom(inputStream);
                Text text = formattedText(
                        resultSet.getInt("id"),
                        resultSet.getInt("locationX"),
                        resultSet.getInt("locationY"),
                        resultSet.getInt("locationZ"),
                        resultSet.getString("worldName"),
                        resultSet.getString("eventtype"),
                        dataContainer,
                        resultSet.getTimestamp("timestamp").toLocalDateTime(),
                        locale
                );
                contents.add(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        preparedStatement.close();
        connection.close();
        return contents;
    }

    public static Text formattedText(int id, int locationx, int locationy, int locationz, String worldname, String eventTypeString, DataContainer dataContainer, LocalDateTime eventDateTime, Locale locale){
        TextTemplate textTemplate = of(
                "ID:",
                arg("ID"),
                " - ",
                arg("eventType"),
                " ",
                "Date: ",
                arg("date"),
                Text.NEW_LINE,
                "Pos: ",
                arg("locx").color(TextColors.RED),
                ", ",
                arg("locy").color(TextColors.BLUE),
                ", ",
                arg("locz").color(TextColors.DARK_GREEN),
                " ",
                arg("cause").optional().color(TextColors.YELLOW),
                " ",
                arg("additionalInfo").optional().color(TextColors.GOLD)
        );

        Map<String, TextElement> textTemplateMap = new TreeMap<>();
        textTemplateMap.put("ID", Text.of(id));
        textTemplateMap.put("eventType", Text.of(eventTypeString));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/LL/YYYY");
        String dateFormatted = eventDateTime.format(formatter);
        textTemplateMap.put("date", Text.of(dateFormatted));
        textTemplateMap.put("locx", Text.of(locationx));
        textTemplateMap.put("locy", Text.of(locationy));
        textTemplateMap.put("locz", Text.of(locationz));

        Text.Builder cause = Text.builder();
        Text.Builder additionalInfo = Text.builder();


        if (dataContainer.contains(DataQuery.of("block"))){
            //add block info
            Optional<BlockSnapshot> blockSnapshotOptional = DataContainerHelper.getBlockSnapshotFromDataContainer(dataContainer.copy());
            if (blockSnapshotOptional.isPresent()){
                BlockSnapshot blockSnapshot = blockSnapshotOptional.get();
                additionalInfo.append(Text.of(blockSnapshot.getState().getType().getTranslation().get(locale)));
            }

        }else if (dataContainer.contains(DataQuery.of("item"))){
            //add item info
            Optional<ItemStackSnapshot> itemStackSnapshotOptional = DataContainerHelper.getItemStackSnapshotFromDataContainer(dataContainer.copy());
            if (itemStackSnapshotOptional.isPresent()){
                ItemStackSnapshot itemStackSnapshot = itemStackSnapshotOptional.get();
                additionalInfo.append(Text.of(itemStackSnapshot.getType().getTranslation().get(locale)));
            }
        }

        if (dataContainer.contains(DataQuery.of("user"))){
            //add player info
            Optional<User> userOptional = DataContainerHelper.getUserFromDataContainer(dataContainer);
            if (userOptional.isPresent()){
                User user = userOptional.get();
                cause.append(Text.of(user.getName()));
            }
        }else if(dataContainer.contains(DataQuery.of("entity"))){
            //add entity info
            Optional<Entity> entityOptional = DataContainerHelper.getEntityFromDataContainer(dataContainer);
            if (entityOptional.isPresent()){
                Entity entity = entityOptional.get();
                cause.append(Text.of(entity.getType().getTranslation().get(locale)));
            }
        }

        textTemplateMap.put("cause", cause);
        textTemplateMap.put("additionalInfo", additionalInfo);
        return textTemplate.apply(textTemplateMap).build();
/*        if (eventType == EventType.BlockBreak){
            Optional<String> playerName = getPlayerName(dataContainer);
            playerName.ifPresent(s -> textTemplateMap.put("cause", Text.of(s)));

            final Optional<DataBuilder<BlockSnapshot>> blockSnapshotDataBuilder = Sponge.getDataManager().getBuilder(BlockSnapshot.class);
            if (blockSnapshotDataBuilder.isPresent()){
                DataBuilder<BlockSnapshot> snapshotDataBuilder =  blockSnapshotDataBuilder.get();
                Optional<DataView> dataViewOptional = dataContainer.getView(DataQuery.of("block", "blocksnapshotDataContainer"));
                if (dataViewOptional.isPresent()){
                    DataView dataView = dataViewOptional.get();
                    Optional<BlockSnapshot> blockSnapshotOptional = snapshotDataBuilder.build(dataView);
                    if (blockSnapshotOptional.isPresent()){
                        BlockSnapshot blockSnapshot = blockSnapshotOptional.get();

                        textTemplateMap.put("additionalInfo", Text.of(blockSnapshot.getState().getType().getTranslation().get(locale)));
                    }
                }
            }
        }else if (eventType == EventType.ConnectionEvent){
            Optional<String> playerName = getPlayerName(dataContainer);
            playerName.ifPresent(s -> textTemplateMap.put("cause", Text.of(s)));
        }else if (eventType == EventType.DisconnectionEvent){
            Optional<String> playerName = getPlayerName(dataContainer);
            playerName.ifPresent(s -> textTemplateMap.put("cause", Text.of(s)));
        }*/
    }

    public static Optional<String> getDataContainerImportantInfo(DataContainer dataContainer, EventType type){
        switch (type){
            case BlockBreak:
                return dataContainer.getString(DataQuery.of("block", "blocksnapshotDataContainer", "BlockState", "BlockState"));
            case ConnectionEvent:
                return dataContainer.getString(DataQuery.of("player", "name"));
            case DisconnectionEvent:
                return dataContainer.getString(DataQuery.of("player", "name"));
        }
        return Optional.empty();
    }
}
