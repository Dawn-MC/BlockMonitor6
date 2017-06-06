package com.amict.blockmonitor;


import com.amict.blockmonitor.Storage.StorageHandler;
import com.amict.blockmonitor.listeners.onChangeBlockEvent;
import com.amict.blockmonitor.listeners.onClientConnectionEvent;
import com.google.inject.Inject;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

@Plugin(
        id = "blockmonitor",
        name = "BlockMonitor"
)
public class BlockMonitor {

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    private File configFile = new File(privateConfigDir + "/BlockMonitor.cfg");

    ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setFile(configFile).build();

    CommentedConfigurationNode configNode;

    public static StorageHandler storageHandler;


    @Listener
    public void onPreInit(GamePreInitializationEvent event){
        storageHandler = new StorageHandler(privateConfigDir);
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        Sponge.getEventManager().registerListeners(this, new onClientConnectionEvent());
        Sponge.getEventManager().registerListeners(this, new onChangeBlockEvent());
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event){
        if (storageHandler != null){
            storageHandler.shutdownHook();
        }
    }

    @Listener
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event){
        System.out.println("player disconnected!");
        try {
            ResultSet rs = storageHandler.dataSource.getConnection().createStatement().executeQuery("SELECT * FROM `blockmonitor`");
            while(rs.next()){
                System.out.println("theres a row!!!");
                System.out.println(rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
