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
import org.jetbrains.exposed.sql.exposedLogger
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.plugin.Dependency
import java.io.File
import java.nio.file.Path


@Plugin(id = "blockmonitorapi", name = "Block Monitor API", description = "A plugin which allows for the monitoring and recording of events in game", version = "1.0.0", dependencies = [Dependency(id = "kotlincore", version = "1.2.10")])
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

        var debugMode: Boolean = false

        var staticLogger: Logger? = null
    }

    @Listener
    fun PreInitEvent(event: GamePreInitializationEvent){

        logger.info(this.javaClass.getAnnotation(Plugin::class.java).name + "has entered PreInit")
        //todo init the database handler
        var configNode = configManager.load()

        if (configNode.isVirtual){
            logger.info("Generating new configs")
            //Generate default config
            configNode.getNode("database", "jdbcString").setValue("jdbc:h2:file:{filelocation}").setComment("the jdbc string used to connect to the database")
            configNode.getNode("database", "driver").setValue("org.h2.Driver").setComment("the driver to be used with the database")
            configNode.getNode("machine", "machineId").setValue(1).setComment("Increment for each server connected to a data source")
            configNode.getNode("debug", "enabled").setValue(false).setComment("Intended for development use, enables enhanced debug logging! only use this if you know what your doing.")
            configManager.save(configNode)
        }

        BlockMonitorApi.Companion.databaseHandler = DatabaseHandler(
                configNode.getNode("database", "jdbcString").getString("jdbc:h2:file:{filelocation}").replace("{filelocation}", File(privateConfigDir.toFile(), "block_monitor").toString()),
                configNode.getNode("database", "driver").getString("org.h2.Driver"))

        BlockMonitorApi.Companion.snowflake = Snowflake(configNode.getNode("machine", "machineId").getInt(1))

        BlockMonitorApi.Companion.debugMode = configNode.getNode("debug", "enabled").getBoolean(false)

        BlockMonitorApi.Companion.staticLogger = logger

        logger.info("Debug mode is: " + if(configNode.getNode("debug", "enabled").getBoolean(false)){"Enabled"}else{"Disabled"})
        logger.info(this.javaClass.getAnnotation(Plugin::class.java).name + "completed preinit successfully!")
    }
}