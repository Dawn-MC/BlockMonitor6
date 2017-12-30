package com.dawndevelop

import com.dawndevelop.event.ClientConnectionEve
import com.google.inject.Inject
import org.slf4j.Logger
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text

@Plugin(id = "blockmonitorcore", name = "Block Monitor Core", description = "Gives functionality to Block Monitors api", dependencies = arrayOf(Dependency(id = "blockmonitorapi", version = "1.0.0")))
class BlockMonitorCore {

    @Inject
    lateinit var logger: Logger

    @Listener
    fun ClientConnectionEvent(event: ClientConnectionEvent){
        if(!BlockMonitorApi.databaseHandler.isReady){
            logger.error("database handler hasnt been initalized")
        }

        if (event is ClientConnectionEvent.Join){
            val joinEvent = event

            BlockMonitorApi.databaseHandler.Insert(ClientConnectionEve.Join(joinEvent.message, joinEvent.targetEntity))
        }else if(event is ClientConnectionEvent.Disconnect){
            val exitEvent = event

            BlockMonitorApi.databaseHandler.Insert(ClientConnectionEve.Disconnect(exitEvent.targetEntity))
        }
    }
}