package com.dawndevelop.event

import com.dawndevelop.BlockMonitorApi
import com.dawndevelop.helpers.DatacontainerHelper
import org.spongepowered.api.block.BlockSnapshot
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.Transaction
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.item.inventory.ItemStackSnapshot
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

class Eventt(cause: Cause, event: org.spongepowered.api.event.Event, others: List<Any>) : Event() {
    init {

        this.Type = EventType.fromSEvent(event).toString()

        if (cause.containsType(Player::class.java)){
            if (this.Location == null)
                this.Location = cause.first(Player::class.java).get().location

            DatacontainerHelper.setPlayer(this.DataContainer, cause.first(Player::class.java).get())
        }
        if (cause.containsType(Entity::class.java)){
            if (this.Location == null)
                this.Location = cause.first(Entity::class.java).get().location

            DatacontainerHelper.setEntity(this.DataContainer, cause.first(Entity::class.java).get())
        }
        if (cause.containsType(org.spongepowered.api.world.Location::class.java)){
            this.Location = cause.first(org.spongepowered.api.world.Location::class.java).get()
        }

        for (other in others){
            when(other){
                is Text -> {DataContainer.set(DataQuery.of("Text"), other.toContainer())}
                is Location<*> -> {
                    if (other.extent is World){
                        this.Location = other as Location<World>
                    }
                }
                is List<*> -> {
                    if(other.isNotEmpty()){
                        if (other.first() is Transaction<*>) {
                            val transaction: Transaction<*> = other.first() as Transaction<*>
                            if (transaction.original is BlockSnapshot && transaction.final is BlockSnapshot){
                                val blockTransactions: List<Transaction<BlockSnapshot>> = other as List<Transaction<BlockSnapshot>>
                                if (blockTransactions.first().default.location.isPresent)
                                    this.Location = blockTransactions.first().default.location.get()

                                DatacontainerHelper.setBlockTransactions(this.DataContainer, blockTransactions)
                            }else if(transaction.original is ItemStackSnapshot && transaction.final is ItemStackSnapshot){
                                val itemTransactions: List<Transaction<ItemStackSnapshot>> = other as List<Transaction<ItemStackSnapshot>>

                                DatacontainerHelper.setItemStackSnapshotTransactions(this.DataContainer, itemTransactions)
                            }
                        }
                    }
                }
            }
        }

        BlockMonitorApi.databaseHandler.Insert(this)
    }
}