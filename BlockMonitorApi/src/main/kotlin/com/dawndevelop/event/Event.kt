package com.dawndevelop.event

import com.dawndevelop.BlockMonitorApi
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.time.Instant
import java.util.*

open class Event {
    init {
        this.setup()
    }

    open fun setup(){
        this.ID = BlockMonitorApi.snowflake.next()
        this.Date = java.util.Date.from(Instant.now())
    }

    constructor(id: Long, date: Date){
        this.ID = id
        this.Date = date
    }

    var ID: Long

    lateinit var Location: Location<World>

    var Date: Date

    var Type: String = "Uknown"

    lateinit var DataContainer: DataContainer

    init {
        this.ID = BlockMonitorApi.snowflake.next()
        this.Date = java.util.Date.from(Instant.now())


    }

    constructor(){
        this.setup()
    }
}