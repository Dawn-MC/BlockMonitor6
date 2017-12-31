package com.dawndevelop.helpers

import com.dawndevelop.BlockMonitorCore
import com.dawndevelop.event.Event
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.TextTemplate
import org.spongepowered.api.text.TextTemplate.arg
import org.spongepowered.api.text.TextTemplate.of
import org.spongepowered.api.text.format.TextColors
import org.spongepowered.api.world.World

class TextRenderer() {

    companion object {
        public val idTemplate: TextTemplate = of(TextColors.BLACK, "Event ID: ", TextColors.RED, arg("eventId"))

        public val eventTypeTemplate: TextTemplate = of(TextColors.BLACK, "Event type: ", TextColors.RED, arg("eventType"))

        public val locationTemplate: TextTemplate = of(TextColors.BLACK, "Location: ", "X: ", TextColors.RED, arg("locX"),
                TextColors.BLACK, " Y: ", TextColors.RED, arg("locY"), TextColors.BLACK, " Z: ", TextColors.RED,
                arg("locZ"), TextColors.BLACK, " World: ", TextColors.RED, arg("locWorld"))

        public val playerTemplate: TextTemplate = of(TextColors.BLACK, "Username: ", TextColors.RED,
                arg("playerName"), TextColors.BLACK, " UUID: ", TextColors.RED, arg("playerUuid"))
        public val entityTemplate: TextTemplate = of(TextColors.BLACK,  "Entity type: ", TextColors.RED,
                arg("entityType"))
        public val blockSnapshotTemplate: TextTemplate = of("not implemented yet")
        public val itemSnapshotTemplate: TextTemplate = of("not implemented yet")

        public val transactionTemplate: TextTemplate = of(TextColors.BLACK, "Original: ", TextColors.RED ,
                arg("originalTransaction"), TextColors.BLACK, " Final: ", TextColors.RED, arg("finalTransaction"))


        public fun renderEvent(event: Event) : Text {
            val textBuilder: Text.Builder = Text.builder()
            textBuilder.append(idTemplate.apply(mapOf(Pair("eventId", event.ID))).build())
            textBuilder.append(eventTypeTemplate.apply(mapOf(Pair("eventType", event.Type))).build())
            textBuilder.append(Text.NEW_LINE)

            var worldName: String = event.Location?.extent?.uniqueId.toString() ?: "Unknown"
            if (event.Location?.extent is World){
                worldName = (event.Location?.extent as World).name
            }

            textBuilder.append(locationTemplate.apply(mapOf(Pair("locX", event.Location?.blockX),
                    Pair("locY", event.Location?.blockY),
                    Pair("locZ", event.Location?.blockZ),
                    Pair("locWorld", worldName))
            ).build())
            textBuilder.append(Text.NEW_LINE)

            if (DatacontainerHelper.containsPlayer(event.DataContainer)){
                val playerOpt = DatacontainerHelper.getPlayer(event.DataContainer)
                if (playerOpt.isPresent){
                    BlockMonitorCore.staticLogger.info("player found")
                    val player = playerOpt.get()
                    textBuilder.append(playerTemplate.apply(mapOf(Pair("playerName", player.name), Pair("playerUuid", player.uniqueId.toString()))).build())
                    textBuilder.append(Text.NEW_LINE)
                }
            }
            if (DatacontainerHelper.containsEntity(event.DataContainer)){
                val entityOpt = DatacontainerHelper.getEntity(event.DataContainer)
                if (entityOpt.isPresent){
                    BlockMonitorCore.staticLogger.info("entity found")
                    val entity = entityOpt.get()
                    textBuilder.append(entityTemplate.apply(mapOf(Pair("entityType", entity.type.toString()))).build())
                    textBuilder.append(Text.NEW_LINE)
                }
            }
            if (DatacontainerHelper.containsBlockTransactions(event.DataContainer)){
                val blockTransactionsOpt = DatacontainerHelper.getBlockTransactions(event.DataContainer)
                if (blockTransactionsOpt.isPresent){
                    BlockMonitorCore.staticLogger.info("blocktransactions found")
                    val blockTransactions = blockTransactionsOpt.get()
                    for (blockTransaction in blockTransactions){
                        BlockMonitorCore.staticLogger.info("blocktransaction found!: 1")
                        textBuilder.append(transactionTemplate.apply(mapOf(Pair("originalTransaction", blockSnapshotTemplate.apply().build()), Pair("finalTransaction", blockSnapshotTemplate.apply().build())) ).build())
                        textBuilder.append(Text.NEW_LINE)
                    }
                }
            }
            if (DatacontainerHelper.containsItemStackSnapshotTransactions(event.DataContainer)){
                val itemStackTransactionsOpt = DatacontainerHelper.getItemStackSnapshotTransactions(event.DataContainer)
                if (itemStackTransactionsOpt.isPresent){
                    val itemStackTransactions = itemStackTransactionsOpt.get()
                    for (itemStackTransaction in itemStackTransactions){
                        textBuilder.append(transactionTemplate.apply(mapOf(Pair("originalTransaction", itemSnapshotTemplate.apply().build()), Pair("finalTransaction", blockSnapshotTemplate.apply().build())) ).build())
                        textBuilder.append(Text.NEW_LINE)
                    }
                }
            }

            return textBuilder.build()
        }
    }

    /*
    companion object {

        public val basicTextTemplate: TextTemplate = TextTemplate.of(
                Text.of(Text.of("Event ID: "), TextColors.RED,
                        arg("eventId"), TextColors.BLACK,
                        Text.of(" Event type: "), TextColors.RED,
                        arg("eventType"), TextColors.BLACK,
                        Text.NEW_LINE,
                        Text.of("Location: "), TextColors.RED,
                        Text.of("X: ${arg("eventLocX")} Y: " +
                                "${arg("eventLocY")} Z: " +
                                "${arg("eventLocY")} World: " +
                                arg("eventLocWorld"))),
                        Text.NEW_LINE)

        public fun RenderTexts(events: List<Event>){

        }

        public fun RenderText(event: Event) : Text {
            when(event){
                is EventChangeBlock -> {
                    var dataView: DataView? = null

                    if (event.DataContainer.get(DataQuery.of("Entity")).isPresent){
                        dataView = event.DataContainer.get(DataQuery.of("Entity")).get() as DataView
                    }

                    var entity: Entity? = null
                    var player: Player? = null

                    if (dataView != null){
                        var entityOpt = Sponge.getDataManager().deserialize(Entity::class.java, dataView)
                        var playerOpt = Sponge.getDataManager().deserialize(Player::class.java, dataView)
                        if (entityOpt.isPresent){
                            entity = entityOpt.get()
                        }else if(playerOpt.isPresent){
                            player = playerOpt.get()
                        }
                    }

                    var textArgs: MutableMap<String, Any> = mutableMapOf()
                    textArgs.put("eventId", event.ID)
                    textArgs.put("eventType", event.Type)
                    textArgs.put("eventLocX", event.Location?.blockX ?: "Unknown")
                    textArgs.put("eventLocY", event.Location?.blockY ?: "Unknown")
                    textArgs.put("eventLocZ", event.Location?.blockZ ?: "Unknown")
                    textArgs.put("eventLocWorld", event.Location?.extent?.name ?: "Unknown")

                    var textBuilder: Text.Builder = basicTextTemplate.apply(textArgs)


                    if (entity != null){
                        textBuilder.append(Text.of(TextColors.BLACK, Text.of(" Entity: "),
                                TextColors.RED, Text.of(entity.type.toString())))
                    } else if(player != null){
                        textBuilder.append(Text.of(TextColors.BLACK, Text.of(" PlayerName: "),
                                TextColors.RED, Text.of(player.name)))
                    }

                    return textBuilder.build()
                }

                is EventClientConnection -> {
                    var textArgs: MutableMap<String, Any> = mutableMapOf()
                    textArgs.put("eventId", event.ID)
                    textArgs.put("eventType", event.Type)
                    textArgs.put("eventLocX", event.Location?.blockX ?: "Unknown")
                    textArgs.put("eventLocY", event.Location?.blockY ?: "Unknown")
                    textArgs.put("eventLocZ", event.Location?.blockZ ?: "Unknown")
                    textArgs.put("eventLocWorld", event.Location?.extent?.name ?: "Unknown")
                    var textBuilder: Text.Builder = basicTextTemplate.apply(textArgs)
                    var player: Player? = null

                }

                else -> Text.of(TextColors.RED, Text.of("Event unknown!"))
            }

            return Text.EMPTY
        }
    }
    */
}