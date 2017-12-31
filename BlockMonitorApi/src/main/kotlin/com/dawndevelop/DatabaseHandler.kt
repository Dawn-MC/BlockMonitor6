package com.dawndevelop

import com.dawndevelop.event.DatabaseEvent
import com.dawndevelop.event.DatabaseEvent.locationX
import com.dawndevelop.event.DatabaseEvent.locationY
import com.dawndevelop.event.DatabaseEvent.locationZ
import com.dawndevelop.event.DatabaseEvent.worldId
import com.dawndevelop.event.Event
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.persistence.DataFormats
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.sql.ResultSet
import java.time.Instant
import java.util.*
import java.util.Date

public class DatabaseHandler (url: String, driver: String) {

    val url: String = url
    val driver: String = driver

    var isReady: Boolean = false

    init {
        BlockMonitorApi.staticLogger?.info("URL: $url\n Driver: $driver")

        Database.connect(url, driver)

        transaction {
            if (BlockMonitorApi.debugMode){
                logger.addLogger(StdOutSqlLogger)
            }
            create(DatabaseEvent)
        }

        isReady = true
    }

    fun Insert(event: Event) {

        if (BlockMonitorApi.debugMode){
            BlockMonitorApi.staticLogger?.debug("A insert event has been called. Type: ${event.Type}\nID: ${event.ID}")
            if (event.Location == null){
                BlockMonitorApi.staticLogger?.debug("Location was null")
            }
        }



        //Database.connect(url, driver)
        Database.connect(url, driver)
        transaction {
            if (BlockMonitorApi.debugMode){
                logger.addLogger(StdOutSqlLogger)
            }

            DatabaseEvent.insert {
                it[id] = event.ID
                it[type] = event.Type
                it[date] = ToJodaTime(event.Date)
                it[dataContainer] = DataFormats.JSON.write(event.DataContainer)
                it[locationX] = event.Location?.blockX ?: 0
                it[locationY] = event.Location?.blockY ?: 0
                it[locationZ] = event.Location?.blockZ ?: 0
                it[worldId] = event.Location?.extent?.uniqueId.toString() ?: "Unknown"
            }
        }
    }

    fun Delete(id: Long){
        Database.connect(url, driver)
        transaction {
            if (BlockMonitorApi.debugMode){
                logger.addLogger(StdOutSqlLogger)
            }
            DatabaseEvent.deleteWhere {
                DatabaseEvent.id eq id
            }
        }
    }

    fun DeleteAll(){
        Database.connect(url, driver)

        transaction {
            DatabaseEvent.deleteAll()
        }
    }

    /**
     * deletes all invalid data and data before the date below
     */
    fun CleanDataSet(date: Date){

        val worldIds: MutableList<String> = emptyList<String>().toMutableList()


        Sponge.getServer().worlds.forEach {
            world: World? -> if (world != null){
                worldIds.add(world.uniqueId.toString())
            }
        }

        Database.connect(url, driver)

        transaction {
            if (BlockMonitorApi.debugMode){
                logger.addLogger(StdOutSqlLogger)
            }
            DatabaseEvent.deleteWhere {
                DatabaseEvent.date lessEq ToJodaTime(date) or
                        DatabaseEvent.worldId.notInList(worldIds)
            }
        }
    }

    fun SelectAllInRange(centerLocation: Location<World>, radius: Int): List<Event> {
        val startX: Int = centerLocation.blockX - radius
        val endX: Int = centerLocation.blockX + radius

        val startY: Int = centerLocation.blockY - radius
        val endY: Int = centerLocation.blockY + radius

        val startZ: Int = centerLocation.blockZ - radius
        val endZ: Int = centerLocation.blockZ + radius

        Database.connect(url, driver)

        return SelectInArea(startX, endX, startY, endY, startZ, endZ, centerLocation.extent.uniqueId.toString())
    }

    fun SelectAllInArea(centerLocation: Location<World>, radiusX: Int, radiusY: Int, radiusZ: Int) : List<Event> {

        val startX: Int = centerLocation.blockX - radiusX
        val endX: Int = centerLocation.blockX + radiusX

        val startY: Int = centerLocation.blockY - radiusY
        val endY: Int = centerLocation.blockY + radiusY

        val startZ: Int = centerLocation.blockZ - radiusZ
        val endZ: Int = centerLocation.blockZ + radiusZ

        Database.connect(url, driver)


        return SelectInArea(startX, endX, startY, endY, startZ, endZ, centerLocation.extent.uniqueId.toString())

    }

    fun SelectInArea(startX: Int, endX: Int, startY: Int, endY: Int, startZ: Int, endZ: Int, worldID: String) : List<Event> {
        Database.connect(url, driver)

        val events: MutableList<Event> = mutableListOf()
        transaction {
            if (BlockMonitorApi.debugMode){
                logger.addLogger(StdOutSqlLogger)
            }

            for(dbEvent in DatabaseEvent.select(
                    ((locationX greaterEq startX) and (locationX lessEq endX)) and
                    ((locationY greaterEq startY) and (locationY lessEq endY)) and
                    ((locationZ greaterEq startZ) and (locationZ lessEq endZ)) and
                    (worldId eq worldID))) {
                val event: Event = Event()
                println("event found id: ${dbEvent[DatabaseEvent.id]}")
                event.ID = dbEvent[DatabaseEvent.id]
                event.Type = dbEvent[DatabaseEvent.type]
                event.Date = ToJavaDate(dbEvent[DatabaseEvent.date])
                event.DataContainer = DataFormats.JSON.read(dbEvent[DatabaseEvent.dataContainer])
                if(dbEvent[DatabaseEvent.worldId] != "null"){
                    BlockMonitorApi.staticLogger?.debug(dbEvent[DatabaseEvent.worldId])
                    if (Sponge.getServer().getWorld(UUID.fromString(dbEvent[DatabaseEvent.worldId])).isPresent)
                        event.Location = Location<World>(Sponge.getServer().getWorld(UUID.fromString(dbEvent[DatabaseEvent.worldId])).get(), dbEvent[DatabaseEvent.locationX], dbEvent[DatabaseEvent.locationY], dbEvent[DatabaseEvent.locationZ])
                    else
                        event.Location = null
                }
                else
                    event.Location = null

                events.add(event)

            }
        }
        return events.toList()
    }

    public fun SelectAll(): List<Event> {
        Database.connect(url, driver)
        val events: MutableList<Event> = mutableListOf()
        transaction {
            if (BlockMonitorApi.debugMode){
                logger.addLogger(StdOutSqlLogger)
            }

            for(dbEvent in DatabaseEvent.selectAll()){
                val event: Event = Event()
                println("event found id: ${dbEvent[DatabaseEvent.id]}")
                event.ID = dbEvent[DatabaseEvent.id]
                event.Type = dbEvent[DatabaseEvent.type]
                event.Date = ToJavaDate(dbEvent[DatabaseEvent.date])
                event.DataContainer = DataFormats.JSON.read(dbEvent[DatabaseEvent.dataContainer])
                if(dbEvent[DatabaseEvent.worldId] != "null"){
                    BlockMonitorApi.staticLogger?.debug(dbEvent[DatabaseEvent.worldId])
                    if (Sponge.getServer().getWorld(UUID.fromString(dbEvent[DatabaseEvent.worldId])).isPresent)
                        event.Location = Location<World>(Sponge.getServer().getWorld(UUID.fromString(dbEvent[DatabaseEvent.worldId])).get(), dbEvent[DatabaseEvent.locationX], dbEvent[DatabaseEvent.locationY], dbEvent[DatabaseEvent.locationZ])
                    else
                        event.Location = null
                }
                else
                    event.Location = null

                events.add(event)

            }
        }
        return events.toList()
    }

    fun ToDatabaseEvents(resultSet: ResultSet?) : Optional<List<Event>> {
        if(resultSet != null){
            val events: List<Event> = emptyList()

            while (resultSet.next()){
                val event: Event = Event()
                event.ID = resultSet.getLong("id")
                event.DataContainer = DataFormats.JSON.read(resultSet.getString("eventDataContainer"))
                event.Date = resultSet.getDate("eventDate")
                event.Type = resultSet.getString("eventType")

                if (!resultSet.getString("eventLocationWorldId").equals("Unknown", true)){
                    val world = Sponge.getServer().getWorld(UUID.fromString(resultSet.getString("eventLocationWorldId")))

                    if (world.isPresent){
                        event.Location = Location<World>(world.get(), resultSet.getInt("eventLocationX"), resultSet.getInt("eventLocationY"), resultSet.getInt("eventLocationZ"))
                    }else{
                        event.Location = null
                    }
                }

            }

            if (events.isEmpty())
                return Optional.empty()
            return Optional.of(events)
        }else{
            return Optional.empty()
        }
    }

    fun ToJodaTime(date: Date): DateTime {
        return DateTime(org.joda.time.Instant(date.toInstant().toEpochMilli()))
    }

    fun ToJavaDate(dateTime: DateTime): Date{
        return Date.from(Instant.ofEpochMilli(dateTime.millis))
    }
}