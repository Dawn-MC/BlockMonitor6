package com.dawndevelop.helpers

import com.dawndevelop.BlockMonitorCore
import jdk.nashorn.internal.ir.Block
import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockSnapshot
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.DataView
import org.spongepowered.api.data.Transaction
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.ItemStackSnapshot
import java.util.*

class DatacontainerHelper {
    companion object {

        fun setPlayer (dataContainer: DataContainer, player: Player) : DataContainer{
            dataContainer.set(DataQuery.of("Player"), player.toContainer())
            return dataContainer
        }

        fun containsPlayer(dataContainer: DataContainer) : Boolean {
            return dataContainer.contains(DataQuery.of("Player"))
        }

        fun getPlayer (dataContainer: DataContainer) : Optional<Player> {
            val playerOpt = dataContainer.getView(DataQuery.of("Player"))
            if (playerOpt.isPresent){
                val playerCont = playerOpt.get()
                return Sponge.getDataManager().deserialize(Player::class.java, playerCont)
            }
            return Optional.empty()
        }

        fun setEntity (dataContainer: DataContainer, entity: Entity) : DataContainer{
            dataContainer.set(DataQuery.of("Entity"), entity.toContainer())
            return dataContainer
        }

        fun containsEntity(dataContainer: DataContainer) : Boolean {
            return dataContainer.contains(DataQuery.of("Entity"))
        }

        fun getEntity (dataContainer: DataContainer) : Optional<Entity> {
            val entityOpt = dataContainer.getView(DataQuery.of("Entity"))
            if (entityOpt.isPresent){
                val entityCont = entityOpt.get()
                return  Sponge.getDataManager().deserialize(Entity::class.java, entityCont)
            }
            return Optional.empty()
        }

        fun setBlockTransactions (dataContainer: DataContainer, transactionList :List<Transaction<BlockSnapshot>>) : DataContainer {
            var dataViews: MutableList<DataView> = mutableListOf()
            for ((id, transaction) in transactionList.withIndex()){
                var dataCon: DataContainer = DataContainer.createNew()
                dataCon.set(DataQuery.of( "Original"), transaction.original.toContainer()).set(DataQuery.of("Final"), transaction.final.toContainer())
                dataViews.add(dataCon)
            }
            dataContainer.set(DataQuery.of("BlockTransactions"), dataViews)
            return dataContainer
        }

        fun containsBlockTransactions(dataContainer: DataContainer) : Boolean {
            return dataContainer.contains(DataQuery.of("BlockTransactions"))
        }

        fun getBlockTransactions (dataContainer: DataContainer) :  Optional<List<Transaction<BlockSnapshot>>>{

            if (containsBlockTransactions(dataContainer)){
                val transactionBlocks = mutableListOf<Transaction<BlockSnapshot>>()
                val viewList = dataContainer.getViewList(DataQuery.of("BlockTransactions"))
                if (viewList.isPresent){
                    for (view in  viewList.get()){
                        if (view.contains(DataQuery.of("Original")) && view.contains(DataQuery.of("Final"))) {
                            val originalOpt = Sponge.getDataManager().deserialize(BlockSnapshot::class.java, view.getView(DataQuery.of("Original")).get())
                            val finalOpt = Sponge.getDataManager().deserialize(BlockSnapshot::class.java, view.getView(DataQuery.of("Final")).get())
                            if (originalOpt.isPresent && finalOpt.isPresent){
                                transactionBlocks.add(Transaction(originalOpt.get(), finalOpt.get()))
                            }
                        }
                    }
                }
                return Optional.of(transactionBlocks.toList())
            }

            return Optional.empty()
        }

        fun setItemStackSnapshotTransactions (dataContainer: DataContainer, transactionList :List<Transaction<ItemStackSnapshot>>) : DataContainer {
            var dataViews: MutableList<DataView> = mutableListOf()
            for ((id, transaction) in transactionList.withIndex()){
                var dataCon: DataContainer = DataContainer.createNew()
                dataCon.set(DataQuery.of( "Original"), transaction.original.toContainer()).set(DataQuery.of("Final"), transaction.final.toContainer())
                dataViews.add(dataCon)
            }
            dataContainer.set(DataQuery.of("ItemStackSnapshots"), dataViews)
            return dataContainer
        }

        fun containsItemStackSnapshotTransactions(dataContainer: DataContainer) : Boolean {
            return dataContainer.contains(DataQuery.of("ItemStackSnapshots"))
        }

        fun getItemStackSnapshotTransactions (dataContainer: DataContainer) :  Optional<List<Transaction<ItemStackSnapshot>>>{

            if (containsBlockTransactions(dataContainer)){
                val transactionBlocks = mutableListOf<Transaction<ItemStackSnapshot>>()
                val viewList = dataContainer.getViewList(DataQuery.of("ItemStackSnapshots"))
                if (viewList.isPresent){
                    for (view in  viewList.get()){
                        if (view.contains(DataQuery.of("Original")) && view.contains(DataQuery.of("Final"))) {
                            val originalOpt = Sponge.getDataManager().deserialize(ItemStackSnapshot::class.java, view.getView(DataQuery.of("Original")).get())
                            val finalOpt = Sponge.getDataManager().deserialize(ItemStackSnapshot::class.java, view.getView(DataQuery.of("Final")).get())
                            if (originalOpt.isPresent && finalOpt.isPresent){
                                transactionBlocks.add(Transaction(originalOpt.get(), finalOpt.get()))
                            }
                        }
                    }
                }
                return Optional.of(transactionBlocks.toList())
            }

            return Optional.empty()
        }
    }
}