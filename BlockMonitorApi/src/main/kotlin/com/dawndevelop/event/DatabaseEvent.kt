package com.dawndevelop.event

import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import org.spongepowered.api.data.DataContainer

object DatabaseEvent : Table() {

    var id: Column<Long> = long("id").default(0).primaryKey().uniqueIndex()

    var type: Column<String> = varchar("eventType", 50).default("Unknown")

    val date: Column<DateTime> = date("eventDate").default(DateTime.now())

    val dataContainer: Column<String> = text("eventDataContainer").default("{}")

    val locationX: Column<Int> = integer("eventLocationX").default(0)

    val locationY: Column<Int> = integer("eventLocationY").default(0)

    val locationZ: Column<Int> = integer("eventLocationZ").default(0)

    val worldId: Column<String> = text("eventLocationWorldId").default("Unknown")
}