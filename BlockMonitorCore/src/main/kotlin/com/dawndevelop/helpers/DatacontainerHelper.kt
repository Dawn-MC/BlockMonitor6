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
                BlockMonitorCore.staticLogger.info("block transactions found")
                val transactionBlocks = mutableListOf<Transaction<BlockSnapshot>>()
                val viewList = dataContainer.getViewList(DataQuery.of("BlockTransactions"))
                if (viewList.isPresent){
                    BlockMonitorCore.staticLogger.info("view list was present")
                    for (view in  viewList.get()){
                        BlockMonitorCore.staticLogger.info("running a view")
                        if (view.contains(DataQuery.of("Original")) && view.contains(DataQuery.of("Final"))) {
                            BlockMonitorCore.staticLogger.info("View contained both original and final data querys")
                            val originalOpt = Sponge.getDataManager().deserialize(BlockSnapshot::class.java, view.getView(DataQuery.of("Original")).get())
                            val finalOpt = Sponge.getDataManager().deserialize(BlockSnapshot::class.java, view.getView(DataQuery.of("Final")).get())
                            if (originalOpt.isPresent && finalOpt.isPresent){
                                BlockMonitorCore.staticLogger.info("Loaded final and original opts into the original class")
                                transactionBlocks.add(Transaction(originalOpt.get(), finalOpt.get()))
                            }
                        }
                    }
                }
                return Optional.of(transactionBlocks.toList())
            }

            /*
            if (dataContainer.contains(DataQuery.of("BlockTransactions"))) {
                var idOpt = dataContainer.getInt(DataQuery.of("maxId"))
                val transactions: MutableList<Transaction<BlockSnapshot>> = mutableListOf()
                if (idOpt.isPresent){
                    var id = idOpt.get()
                    while (id >= 0){
                        val transactionOrignalOpt = dataContainer.getView(DataQuery.of("BlockTransactions", id.toString(), "Original"))
                        val transactionFinalOpt = dataContainer.getView(DataQuery.of("BlockTransactions", id.toString(), "Final"))
                        if (transactionOrignalOpt.isPresent && transactionFinalOpt.isPresent){
                            BlockMonitorCore.staticLogger.info("transaction views found")

                            val transOrginalCont = transactionOrignalOpt.get()
                            val transFinalCont = transactionFinalOpt.get()
                            val transOriginal = Sponge.getDataManager().deserialize(BlockSnapshot::class.java, transOrginalCont)
                            val transFinal = Sponge.getDataManager().deserialize(BlockSnapshot::class.java, transFinalCont)

                            if (transOriginal.isPresent && transFinal.isPresent){
                                BlockMonitorCore.staticLogger.info("transaction creation in progress")
                                transactions.add(Transaction(transOriginal.get(), transFinal.get()))
                            }

                        }
                        id--
                    }

                    return Optional.of(transactions.toList())
                }
                return Optional.empty()
            }
            */

            return Optional.empty()
        }

        /*
        fun setBlockTransactions (dataContainer: DataContainer, transactionList :List<Transaction<BlockSnapshot>>) : DataContainer {
            for ((id, transaction) in transactionList.withIndex()){
                dataContainer.set(DataQuery.of("BlockTransactions", id.toString()), transaction.toContainer())
                dataContainer.set(DataQuery.of("BlockTransactions", "maxId"), id)
            }
            return dataContainer
        }

        fun containsBlockTransactions(dataContainer: DataContainer) : Boolean {
            return dataContainer.contains(DataQuery.of("BlockTransactions"))
        }

        fun getBlockTransactions (dataContainer: DataContainer) : Optional<List<Transaction<BlockSnapshot>>> {
            if (dataContainer.contains(DataQuery.of("BlockTransactions"))){
                val maxIdOpt = dataContainer.getInt(DataQuery.of("BlockTransactions", "maxId"))
                if (maxIdOpt.isPresent){
                    var maxId = maxIdOpt.get()
                    val blockTransactions: MutableList<Transaction<BlockSnapshot>> = mutableListOf()
                    while (maxId >= 0){
                        val dataView = dataContainer.getView(DataQuery.of("BlockTransactions", maxId.toString()))
                        if (dataView.isPresent){
                            val blockTransactionOpt = Sponge.getDataManager().deserialize(Transaction::class.java, dataView.get())
                            if (blockTransactionOpt.isPresent){
                                if (blockTransactionOpt.get().default is BlockSnapshot && blockTransactionOpt.get().final is BlockSnapshot){
                                    BlockMonitorCore.staticLogger.info("Blocksnapshot transaction found")
                                    val blockTransaction1 = Transaction<BlockSnapshot>(blockTransactionOpt.get().default as BlockSnapshot, blockTransactionOpt.get().final as BlockSnapshot)
                                    blockTransactions.add(blockTransaction1)
                                }
                            }
                        }

                        maxId--
                    }

                    return Optional.of(blockTransactions)
                }
            }

            return Optional.empty()
        }
*/

        fun setItemStackTransactions (dataContainer: DataContainer, transactionList :List<Transaction<ItemStackSnapshot>>) : DataContainer {
            for ((id, transaction) in transactionList.withIndex()){
                dataContainer.set(DataQuery.of("ItemTransactions", id.toString()), transaction.toContainer())
                dataContainer.set(DataQuery.of("ItemTransactions", "maxId"), id)
            }
            return dataContainer
        }

        fun containsItemStackTransactions (dataContainer: DataContainer) : Boolean {
            return dataContainer.contains(DataQuery.of("ItemTransactions"))
        }

        fun getItemStackTransactions (dataContainer: DataContainer) : Optional<List<Transaction<ItemStackSnapshot>>> {
            if (dataContainer.contains(DataQuery.of("ItemTransactions"))){
                val maxIdOpt = dataContainer.getInt(DataQuery.of("ItemTransactions", "maxId"))
                if (maxIdOpt.isPresent){
                    var maxId = maxIdOpt.get()
                    val itemTransactions: MutableList<Transaction<ItemStackSnapshot>> = mutableListOf()
                    while (maxId >= 0){
                        val dataView = dataContainer.getView(DataQuery.of("ItemTransactions", maxId.toString()))
                        if (dataView.isPresent){
                            val itemTransactionOpt = Sponge.getDataManager().deserialize(Transaction::class.java, dataView.get())
                            if (itemTransactionOpt.isPresent){
                                if (itemTransactionOpt.get().default is ItemStackSnapshot){
                                    itemTransactions.add(itemTransactionOpt.get() as Transaction<ItemStackSnapshot>)
                                }
                            }
                        }
                        maxId--
                    }
                    return Optional.of(itemTransactions)
                }
            }
            return Optional.empty()
        }
    }
}