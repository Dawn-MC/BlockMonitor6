package com.dawndevelop

import com.google.inject.Inject
import com.relops.snowflake.Snowflake
import org.slf4j.Logger
import org.spongepowered.api.Game
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.plugin.Plugin
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.config.ConfigDir
import java.io.File
import java.nio.file.Path


@Plugin(id = "blockmonitorapi", name = "Block Monitor API", description = "A plugin which allows for the monitoring and recording of events in game", version = "1.0.0")
public class BlockMonitorApi {

    @Inject lateinit var game: Game

    @Inject lateinit var logger: Logger



    @Inject
    @ConfigDir(sharedRoot = false)
    lateinit var privateConfigDir: Path

    @Inject
    @DefaultConfig(sharedRoot = true)
    lateinit var configManager: ConfigurationLoader<CommentedConfigurationNode>

    companion object {

        lateinit var databaseHandler: DatabaseHandler

        lateinit var snowflake: Snowflake
    }

    @Listener
    fun PreInitEvent(event: GamePreInitializationEvent){

        println("entering preinit state")
        //todo init the database handler
        var configNode = configManager.load()
        if (configNode.childrenList.size == 0){
            //Generate default config
            configNode.getNode("database", "jdbcString").setValue("jdbc:h2:file:{filelocation}").setComment("the jdbc string used to connect to the database")
            configNode.getNode("database", "driver").setValue("org.h2.Driver").setComment("the driver to be used with the database")
            configNode.getNode("machine", "machineId").setValue(1).setComment("Increment for each server connected to a data source, ensures all records are unique in some way(specifically by using a snowflake algorithm for IDS")
            configManager.save(configNode)
        }

        BlockMonitorApi.databaseHandler = DatabaseHandler(
                configNode.getNode("database", "jdbcString").getString("jdbc:h2:file:{filelocation}").replace("{filelocation}", File(privateConfigDir.toFile(), "block_monitor").toString()),
                configNode.getNode("database", "driver").getString("org.h2.Driver"))

        BlockMonitorApi.snowflake = Snowflake(configNode.getNode("machine", "machineId").getInt(1))

        logger.info(this.javaClass.getAnnotation(Plugin::class.java).name + "completed preinit successfully!")

    }
    /*
    @Listener
    fun PostInitEvent(event1: GamePostInitializationEvent){

    }

    @Listener
    fun ServerStartedEvent(event1: GameStartedServerEvent){
        logger.info("entering server start phase")

        val event = Event
        event.ID = snowflake.next()
        event.DataContainer = DataContainer.createNew()
        event.Date = Date.from(Instant.now())
        event.Type = "test"
        event.Location = Location(Sponge.getServer().worlds.first(), 0, 0,0)

        databaseHandler.Insert(event)
    }

    @Listener
    fun ClientConEvent(event: ClientConnectionEvent){

    }
    */
}