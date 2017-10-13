package com.dawndevelop.blockmonitor.Storage;

import com.dawndevelop.blockmonitor.api.Event;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class H2Handler implements IStorageHandler {
    private HikariDataSource dataSource;
    @Override
    public void Setup(String InstallPath, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2://" + InstallPath + "/blockmonitor");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(100);
        dataSource = new HikariDataSource(config);

        try {
            System.out.println("create table : " + dataSource.getConnection().createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `blockmonitor` (" +
                            "`id` BIGINT AUTO_INCREMENT NOT NULL," +
                            "`locationX` INT," +
                            "`locationY` INT," +
                            "`locationZ` INT," +
                            "`worldName` VARCHAR(500)," +
                            "`eventtype` VARCHAR(255)," +
                            "`datacontainer` VARCHAR(10000)," +
                            "`timestamp` TIMESTAMP" +
                            ");")
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Insert(Event event) {
        try(Connection conn = dataSource.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement(
                    "INSERT INTO `block_monitor_events` (" +
                            "`id`, " +
                            "`event_type`,"+
                            "`event_information`, " +
                            "`player_information`," +
                            "`block_information`," +
                            "`itemStack_information`," +
                            "`world_location`," +
                            "`timestamp`" +
                            ") VALUES (?,?,?,?,?,?,?,?);"
            );

            preparedStatement.setLong(1, event.getId());
            preparedStatement.setString(2, event.getEventType().name());
            preparedStatement.setString(3, event.getEventInformation());
            preparedStatement.setString(4, event.getPlayerInformation());
            preparedStatement.setString(5, event.getBlockInformation());
            preparedStatement.setString(6, event.getItemStackInformation());
            preparedStatement.setTimestamp(8, event.getTimestamp());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Remove(long ID) {
        try(Connection conn = dataSource.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM `block_monitor_events` WHERE `id` = ?;");
            preparedStatement.setLong(1, ID);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Shutdown() {
        dataSource.close();
    }
}
