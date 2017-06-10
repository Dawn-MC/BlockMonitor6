package com.amict.blockmonitor.utils;

import com.amict.blockmonitor.BlockMonitor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by johnfg10 on 08/06/2017.
 */
public class SearchHelper {
    public static List<Text> searchArea(Location<World> worldLocation, int searchRaduis) throws SQLException {
        int searchDiameter = searchRaduis*2;

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
            contents.add(Text.builder().append(org.spongepowered.api.text.Text.of(String.format("ID:%d-%s ",
                resultSet.getInt("id"),
                resultSet.getString("eventtype")
            )))
                    .append(Text.builder().color(TextColors.RED).append(Text.of("X:" + resultSet.getDouble("locationX") + " ")).build())
                    .append(Text.builder().color(TextColors.BLUE).append(Text.of("Y:" + resultSet.getDouble("locationY") + " ")).build())
                    .append(Text.builder().color(TextColors.GREEN).append(Text.of("Z:" + resultSet.getDouble("locationZ") + " ")).build())
                    .build());
        }

        preparedStatement.close();
        connection.close();
        return contents;
    }

}
