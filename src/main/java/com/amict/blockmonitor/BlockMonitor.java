package com.amict.blockmonitor;


import com.amict.blockmonitor.Storage.StorageHandler;
import com.amict.blockmonitor.commands.onRestoreNear;
import com.amict.blockmonitor.commands.onSearchNear;
import com.amict.blockmonitor.listeners.onChangeBlockEvent;
import com.amict.blockmonitor.listeners.onClientConnectionEvent;
import com.amict.blockmonitor.listeners.onInteractBlockEvent;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(
        id = "blockmonitor",
        name = "BlockMonitor",
        version = "0.1.0",
        description = "A plugin that monitors what players do ingame and logs it!"
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

    public static boolean isInspectEnabled = false;


    @Listener
    public void onPreInit(GamePreInitializationEvent event){
        storageHandler = new StorageHandler(privateConfigDir);
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        CommandSpec onSearchNear = CommandSpec.builder()
                .description(Text.of(""))
                .permission("blockmonitor.search.near")
                .executor(new onSearchNear())
                .build();
        Sponge.getCommandManager().register(this, onSearchNear, "searchnear", "searchn", "sn");

        CommandSpec onRestoreNear = CommandSpec.builder()
                .description(Text.of(""))
                .permission("blockmonitor.restore.near")
                .executor(new onRestoreNear())
                .build();
        Sponge.getCommandManager().register(this, onRestoreNear, "restorenear", "restoren", "rn");
        //Listeners
        Sponge.getEventManager().registerListeners(this, new onClientConnectionEvent());
        Sponge.getEventManager().registerListeners(this, new onChangeBlockEvent());
        Sponge.getEventManager().registerListeners(this, new onInteractBlockEvent());
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event){
        if (storageHandler != null){
            storageHandler.shutdownHook();
        }
    }

    @Listener
    public void onPlayerLeave(ClientConnectionEvent.Disconnect event){
/*        System.out.println("player disconnected!");
        try {
            ResultSet rs = storageHandler.dataSource.getConnection().createStatement().executeQuery("SELECT * FROM `blockmonitor` WHERE `locationX` > 100 AND `locationX` < 200");
            while(rs.next()){
                System.out.println("theres a row!!!");
                System.out.println(rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
    }
}
