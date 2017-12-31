package com.dawndevelop

import com.dawndevelop.event.EventChangeBlock
import com.dawndevelop.event.EventClientConnection
import com.dawndevelop.event.TextRenderer
import com.google.inject.Inject
import org.slf4j.Logger
import org.spongepowered.api.Game
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.ChangeBlockEvent
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.service.pagination.PaginationList
import org.spongepowered.api.text.Text

@Plugin(id = "blockmonitorcore", name = "Block Monitor Core", description = "Gives functionality to Block Monitors api", dependencies = [Dependency(id = "blockmonitorapi", version = "1.0.0"), Dependency(id = "kotlincore", version = "1.2.10")])
class BlockMonitorCore {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var game: Game

    companion object {
        lateinit var staticLogger: Logger
    }

    @Listener
    fun PreInitEvent(event: GamePreInitializationEvent){
        game.commandManager.register(this, CommandSpec.builder()
                .description(Text.of("Deletes all records in the datastore indiscriminately"))
                .executor { source, context ->

                    BlockMonitorApi.databaseHandler.DeleteAll()
                    CommandResult.empty()
                }.build(), "bmdeleteall")

        game.commandManager.register(this, CommandSpec.builder()
                .description(Text.of(""))
                .arguments(GenericArguments.onlyOne(GenericArguments.longNum(Text.of("id"))))
                .executor { source, context ->
                    BlockMonitorApi.databaseHandler.Delete(context.getOne<Long>(Text.of("id")).get())
                    CommandResult.empty()
                }.build(), "bmdelete")

        game.commandManager.register(this, CommandSpec.builder()
                .description(Text.of(""))
                .executor { source, context ->
                    source.sendMessage(Text.of("Searching"))
                    val selectedResults = BlockMonitorApi.databaseHandler.SelectAll()
                    println("Selected results size = ${selectedResults.size}")
                    for (result in selectedResults){
                        source.sendMessage(Text.of(
                                "ID: ${result.ID} EventType: ${result.Type} Location: ${result.Location?.toString() ?: "Location not found!"}"
                        ))
                    }
                    CommandResult.success()
                }.build(), "bmprintall")

        game.commandManager.register(this, CommandSpec.builder()
                .description(Text.of(""))
                .arguments(GenericArguments.onlyOne(GenericArguments.integer(Text.of("radius"))))
                .executor { source, context ->
                    if (source is Player){
                        val results = BlockMonitorApi.databaseHandler.SelectAllInRange(source.location ,context.getOne<Int>(Text.of("radius")).get())
                        val texts: MutableList<Text> = mutableListOf()

                        for (result in results){
                            texts.add(TextRenderer.renderEvent(result))
                        }

                        PaginationList.builder().title(Text.of("Search results")).contents(texts).linesPerPage(20).build().sendTo(source)
                    }
                    CommandResult.empty()
                }.build(), "bmsearchrange")
    }

    @Listener
    fun ClientConnectionEvent(event: ClientConnectionEvent){
        BlockMonitorCore.Companion.staticLogger = logger

        if(!BlockMonitorApi.databaseHandler.isReady){
            logger.error("database handler hasn't been initialized")
        }

        if (event is ClientConnectionEvent.Join){
            BlockMonitorApi.databaseHandler.Insert(EventClientConnection.Join(event.message, event.targetEntity))
        }else if(event is ClientConnectionEvent.Disconnect){
            BlockMonitorApi.databaseHandler.Insert(EventClientConnection.Disconnect(event.targetEntity))
        }
    }

    @Listener
    fun breakBlockEvent(event: ChangeBlockEvent){
        when(event){
            is ChangeBlockEvent.Break -> {
                val breakEvent: ChangeBlockEvent.Break = event
                BlockMonitorApi.databaseHandler.Insert(EventChangeBlock.Break(breakEvent.transactions, breakEvent.cause.first(Entity::class.java)))
            }

            is ChangeBlockEvent.Place -> {
                var placeEvent: ChangeBlockEvent.Place = event
                BlockMonitorApi.databaseHandler.Insert(EventChangeBlock.Place(placeEvent.transactions, placeEvent.cause.first(Entity::class.java)))
            }

            is ChangeBlockEvent.Modify -> {
               var modifyEvent: ChangeBlockEvent.Modify = event
                BlockMonitorApi.databaseHandler.Insert(EventChangeBlock.Modify(modifyEvent.transactions, modifyEvent.cause.first(Entity::class.java)))
            }

            is ChangeBlockEvent.Decay -> {
                var decayEvent: ChangeBlockEvent.Decay = event
                BlockMonitorApi.databaseHandler.Insert(EventChangeBlock.Decay(decayEvent.transactions, decayEvent.cause.first(Entity::class.java)))
            }

            is ChangeBlockEvent.Grow -> {
                var growEvent: ChangeBlockEvent.Grow = event
                BlockMonitorApi.databaseHandler.Insert(EventChangeBlock.Grow(growEvent.transactions, growEvent.cause.first(Entity::class.java)))
            }
        }
    }
}