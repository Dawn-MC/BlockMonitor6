package com.dawndevelop.blockmonitor.Storage;

import com.dawndevelop.blockmonitor.Storage.IStorageHandler;
import com.dawndevelop.blockmonitor.api.Event;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLException;
import java.sql.Statement;

public class MysqlHandler implements IStorageHandler {
    public HikariDataSource dataSource;

    @Override
    public void Setup(String InstallPath, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + "" + "/blockmonitor");
        config.setMaximumPoolSize(100);
        if (username != null)
            config.setUsername(username);
        if (password != null)
            config.setPassword(password);

        dataSource = new HikariDataSource(config);

        try(Statement sqlStatement = dataSource.getConnection().createStatement()) {
            sqlStatement.execute(
                    "CREATE TABLE IF NOT EXISTS `blockmonitor` (" +
                            "`id` BIGINT AUTO_INCREMENT NOT NULL," +
                            "`location` LONGTEXT" +
                            "`datacontainer` LONGTEXT," +
                            "`timestamp` TIMESTAMP" +
                            ");"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Insert(Event event) {

    }

    @Override
    public void Remove(long ID) {

    }

    @Override
    public void Shutdown() {
        dataSource.close();
    }
}
