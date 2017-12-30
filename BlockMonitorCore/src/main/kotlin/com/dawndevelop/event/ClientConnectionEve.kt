package com.dawndevelop.event

import com.dawndevelop.BlockMonitorApi
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.network.Message
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.time.Instant

open class ClientConnectionEve : Event() {
    class Join(msg: Text, player: Player) : ClientConnectionEve() {

        var player: Player = player

        var message: Text = msg

        init {
            this.ID = BlockMonitorApi.snowflake.next()
            this.Date = java.util.Date.from(Instant.now())
            this.Location = this.player.location
            this.Type = EventType.ClientConnectionJoin.toString()

            this.DataContainer = org.spongepowered.api.data.DataContainer.createNew().set(DataQuery.of("player"), this.player.toContainer()).set(DataQuery.of("message"), this.message.toContainer())

        }
    }

    class Disconnect(player: Player) : ClientConnectionEve() {
        var player: Player = player


        init {
            this.ID = BlockMonitorApi.snowflake.next()
            this.Date = java.util.Date.from(Instant.now())
            this.Location = this.player.location
            this.Type = EventType.ClientConnectionJoin.toString()

            this.DataContainer = org.spongepowered.api.data.DataContainer.createNew().set(DataQuery.of("player"), this.player.toContainer())

        }
    }
}