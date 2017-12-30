package com.dawndevelop

import com.dawndevelop.event.DatabaseEvent
import com.dawndevelop.event.DatabaseEvent.locationX
import com.dawndevelop.event.DatabaseEvent.locationY
import com.dawndevelop.event.DatabaseEvent.locationZ
import com.dawndevelop.event.DatabaseEvent.worldId
import com.dawndevelop.event.Event
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.persistence.DataFormats
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.sql.ResultSet
import java.util.*
import java.util.Date

class DatabaseHandler (url: String, driver: String) {

    init {
        connectToDatabase(url, driver)
    }

    lateinit var url: String
    lateinit var driver: String

    lateinit var database: Database

    var isReady: Boolean = false

    private fun connectToDatabase(url1: String, driver1: String){
        url = url1
        driver = driver1

        database = Database.connect(url, driver)

        transaction {
            logger.addLogger(StdOutSqlLogger)

            create(DatabaseEvent)
        }

        isReady = true
    }

    fun Insert(event: Event) {

        Database.connect(url, driver)

        transaction {
            logger.addLogger(Slf4jSqlLogger)

            DatabaseEvent.insert {
                it[id] = event.ID
                it[type] = event.Type
                it[date] = ToJodaTime(event.Date)
                it[dataContainer] = DataFormats.JSON.write(event.DataContainer)
                it[locationX] = event.Location.blockX
                it[locationY] = event.Location.blockY
                it[locationZ] = event.Location.blockZ
                it[worldId] = event.Location.extent.uniqueId.toString()
            }
        }
    }

    fun Delete(id: Long){
        DatabaseEvent.deleteWhere {
            DatabaseEvent.id eq id
        }
    }

    fun DeleteAll(){
        DatabaseEvent.deleteAll()
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


        DatabaseEvent.deleteWhere {
            DatabaseEvent.date lessEq ToJodaTime(date) or
                    DatabaseEvent.worldId.notInList(worldIds)
        }
    }


    fun SelectAllInArea(centerLocation: Location<World>, radiusX: Int, radiusY: Int, radiusZ: Int) : ResultSet?{

        val startX: Int = centerLocation.blockX - radiusX
        val endX: Int = centerLocation.blockX + radiusX

        val startY: Int = centerLocation.blockY - radiusY
        val endY: Int = centerLocation.blockY + radiusY

        val startZ: Int = centerLocation.blockZ - radiusZ
        val endZ: Int = centerLocation.blockZ + radiusZ

        return SelectInArea(startX, endX, startY, endY, startZ, endZ, centerLocation.extent.uniqueId.timestamp())
    }

    fun SelectInArea(startX: Int, endX: Int, startY: Int, endY: Int, startZ: Int, endZ: Int, worldID: Long) : ResultSet? {
        return DatabaseEvent.select{
            ((locationX greaterEq startX) and (locationX lessEq endX)) and
                    ((locationY greaterEq startY) and (locationY lessEq endY)) and
                    ((locationZ greaterEq startZ) and (locationZ lessEq endZ)) and
                    (worldId eq worldID)
        }.execute(TransactionManager.currentOrNew(1))
    }

    fun ToDatabaseEvent(resultSet: ResultSet?) : Optional<List<Event>> {
        if(resultSet != null){
            val events: List<Event> = emptyList()

            while (resultSet.next()){
                var event: Event = Event()
                event.ID = resultSet.getLong("id")
                event.DataContainer = DataFormats.JSON.read(resultSet.getString("eventDataContainer"))
                event.Date = resultSet.getDate("eventDate")
                event.Type = resultSet.getString("eventType")

                val world = Sponge.getServer().getWorld(UUID.fromString(resultSet.getString("eventLocationWorldId")))

                if (world.isPresent){
                    event.Location = Location<World>(world.get(), resultSet.getInt("eventLocationX"), resultSet.getInt("eventLocationY"), resultSet.getInt("eventLocationZ"))
                }else{
                    System.err.println("World could not be found this record will be ignored! Might be advisable running a clearance on the data set")
                    return Optional.empty()
                }
            }

            return Optional.of(events)
        }else{
            return Optional.empty()
        }
    }

    fun ToJodaTime(date: Date): DateTime {
        return DateTime(date)
    }
}