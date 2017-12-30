package com.dawndevelop.event

import com.dawndevelop.BlockMonitorCore
import org.spongepowered.api.block.BlockSnapshot
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.Transaction
import org.spongepowered.api.entity.Entity
import java.util.*

open class EventChangeBlock(_transactions: List<Transaction<BlockSnapshot>>, _entityCause: Optional<Entity>) : Event() {

    private val transactions = _transactions

    private val entity: Entity?

    init {
        for ((transactionNumber, transaction) in transactions.withIndex()){
            this.DataContainer.set(DataQuery.of("blockTransactions_", transactionNumber.toString()), transaction.toContainer())
        }

        if (_entityCause.isPresent){
            entity = _entityCause.get()
            this.Location = entity.location
            this.DataContainer.set(DataQuery.of("Entity"), entity.toContainer())
        }else{

            if (transactions.first().default.location.isPresent) {
                this.Location = transactions.first().default.location.get()
            }else{
                BlockMonitorCore.staticLogger.debug("Location could not be set")
            }
            entity = null
        }
    }

    class Break(transactions: List<Transaction<BlockSnapshot>>, entity: Optional<Entity>) : EventChangeBlock(transactions, entity) {
        init {
            this.Type = EventType.ChangeBlockBreak.toString()
        }
    }

    class Decay(transactions: List<Transaction<BlockSnapshot>>, entity: Optional<Entity>) : EventChangeBlock(transactions, entity) {
        init {
            this.Type = EventType.ChangeBlockDecay.toString()
        }
    }

    class Grow(transactions: List<Transaction<BlockSnapshot>>, entity: Optional<Entity>) : EventChangeBlock(transactions, entity) {
        init {
            this.Type = EventType.ChangeBlockGrow.toString()
        }
    }

    class Modify(transactions: List<Transaction<BlockSnapshot>>, entity: Optional<Entity>) : EventChangeBlock(transactions, entity) {
        init {
            this.Type = EventType.ChangeBlockModify.toString()
        }
    }

    class Place(transactions: List<Transaction<BlockSnapshot>>, entity: Optional<Entity>) : EventChangeBlock(transactions, entity) {
        init {
            this.Type = EventType.ChangeBlockPlace.toString()
        }
    }
}