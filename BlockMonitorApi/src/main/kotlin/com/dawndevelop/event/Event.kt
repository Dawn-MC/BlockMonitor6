package com.dawndevelop.event

import com.dawndevelop.BlockMonitorApi
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.time.Instant
import java.util.*

open class Event {
    constructor(){
        this.ID = BlockMonitorApi.snowflake.next()
        this.Date = java.util.Date.from(Instant.now())
    }

    constructor(id: Long, date: Date){
        this.ID = id
        this.Date = date
    }

    var ID: Long = BlockMonitorApi.snowflake.next()

    var Location: Location<World>? = null

    var Date: Date = java.util.Date.from(Instant.now())

    var Type: String = "Unknown"

    var DataContainer: DataContainer = org.spongepowered.api.data.DataContainer.createNew()
}