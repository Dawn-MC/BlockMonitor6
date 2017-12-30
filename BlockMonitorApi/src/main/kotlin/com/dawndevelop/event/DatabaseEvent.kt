package com.dawndevelop.event

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import org.spongepowered.api.data.DataContainer

object DatabaseEvent : Table() {

    var id: Column<Long> = long("id").default(0).primaryKey().uniqueIndex().autoIncrement()

    var type: Column<String> = varchar("eventType", 50)

    val date: Column<DateTime> = date("eventDate")

    val dataContainer: Column<String> = text("eventDataContainer")

    val locationX: Column<Int> = integer("eventLocationX")

    val locationY: Column<Int> = integer("eventLocationY")

    val locationZ: Column<Int> = integer("eventLocationZ")

    val worldId: Column<String> = text("eventLocationWorldId")
}