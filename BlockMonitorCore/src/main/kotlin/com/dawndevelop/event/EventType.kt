package com.dawndevelop.event

import org.spongepowered.api.event.Event
import org.spongepowered.api.event.block.ChangeBlockEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import java.util.*

enum class EventType {
    ClientConnectionJoin,
    ClientConnectionDisconnect,
    ChangeBlockBreak,
    ChangeBlockPlace,
    ChangeBlockModify,
    ChangeBlockGrow,
    ChangeBlockDecay,
    Unknown;

    companion object {
        fun fromSEvent(event: Event) : EventType {
            return when(event){
                is ClientConnectionEvent.Join -> ClientConnectionJoin
                is ClientConnectionEvent.Disconnect -> ClientConnectionDisconnect
                is ChangeBlockEvent.Break -> ChangeBlockBreak
                is ChangeBlockEvent.Place -> ChangeBlockPlace
                is ChangeBlockEvent.Modify -> ChangeBlockModify
                is ChangeBlockEvent.Grow -> ChangeBlockGrow
                is ChangeBlockEvent.Decay -> ChangeBlockDecay
                else -> Unknown
            }

        }
    }
}