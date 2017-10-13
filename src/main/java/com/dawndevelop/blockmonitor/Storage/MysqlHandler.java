package com.dawndevelop.blockmonitor.Storage;

import com.dawndevelop.blockmonitor.api.Event;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlHandler implements IStorageHandler {
    private HikariDataSource dataSource;

    @Override
    public void Setup(String InstallPath, String username, String password) {
        if (InstallPath == null){
            throw new IllegalArgumentException("Install path must contain the IP address of the mysql server");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + InstallPath + "/blockmonitor");
        config.setMaximumPoolSize(100);

        if (username != null)
            config.setUsername(username);

        if (password != null)
            config.setPassword(password);

        dataSource = new HikariDataSource(config);

        try(Statement sqlStatement = dataSource.getConnection().createStatement()) {
            sqlStatement.execute(
                    "CREATE TABLE IF NOT EXISTS `block_monitor_events` (" +
                            "`id` BIGINT AUTO_INCREMENT NOT NULL," +
                            "`event_type` TEXT," +
                            "`event_information` LONGTEXT," +
                            "`player_information` LONGTEXT," +
                            "`block_information` LONGTEXT," +
                            "`itemStack_information` LONGTEXT," +
                            "`world_location` LONGTEXT," +
                            "`timestamp` TIMESTAMP" +
                            ");"
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
