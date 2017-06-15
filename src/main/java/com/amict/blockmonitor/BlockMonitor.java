package com.amict.blockmonitor;


import com.amict.blockmonitor.Storage.StorageHandler;
import com.amict.blockmonitor.commands.onRestoreNear;
import com.amict.blockmonitor.commands.onSearchNear;
import com.amict.blockmonitor.listeners.*;
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
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private File configFile;

    ConfigurationLoader<CommentedConfigurationNode> configLoader;

    CommentedConfigurationNode configNode;

    public static StorageHandler storageHandler;

    public static ExecutorService executor;

    @Listener
    public void onPreInit(GamePreInitializationEvent event){

    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        storageHandler = new StorageHandler(privateConfigDir);
        configFile = new File(privateConfigDir + File.separator + "main.cfg");
        if (!configFile.exists()){
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            configLoader = HoconConfigurationLoader.builder().setFile(configFile).build();
            configNode = configLoader.createEmptyNode();
            configNode.getNode("config", "version").setValue("0.0.1").setComment("automatic setting no touchy");
            configNode.getNode("threading", "excepool").setValue(10).setComment("The amount of threads used by the thread pool");
            configNode.getNode("modules", "tracking", "inventory").setValue(true).setComment("set to false to disable inventory tracking");
            configNode.getNode("modules", "tracking", "block").setValue(true).setComment("set to false to disable block tracking");
            configNode.getNode("modules", "tracking", "connection").setValue(true).setComment("set to false to disable connection tracking");
            try {
                configLoader.save(configNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            configLoader = HoconConfigurationLoader.builder().setFile(configFile).build();
            try {
                configNode = configLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executor = Executors.newFixedThreadPool(configNode.getNode("threading", "excepool").getInt());

        CommandSpec onSearchNear = CommandSpec.builder()
                .description(Text.of("Searches in a 10 block area!"))
                .permission("blockmonitor.search.near")
                .executor(new onSearchNear())
                .build();
        Sponge.getCommandManager().register(this, onSearchNear, "searchnear", "searchn", "sn");

        CommandSpec onRestoreNear = CommandSpec.builder()
                .description(Text.of("replaces blocks in a 10 block area!"))
                .permission("blockmonitor.restore.near")
                .executor(new onRestoreNear())
                .build();
        Sponge.getCommandManager().register(this, onRestoreNear, "restorenear", "restoren", "rn");
        //Listeners
        if (configNode.getNode("modules", "tracking", "connection").getBoolean())
            Sponge.getEventManager().registerListeners(this, new onClientConnectionEvent());
        if (configNode.getNode("modules", "tracking", "block").getBoolean())
            Sponge.getEventManager().registerListeners(this, new onChangeBlockEvent());
        if (configNode.getNode("modules", "tracking", "inventory").getBoolean())
            Sponge.getEventManager().registerListeners(this, new onInteractInventoryEvent());
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        if (storageHandler != null) {
            storageHandler.shutdownHook();
        }
    }
}
