package com.dawndevelop.event

import java.util.*

enum class EventType {
    ClientConnectionJoin,
    ClientConnectionDisconnect;

    fun toEvent(eventType: EventType): Optional<Event> {
        when(eventType){
            ClientConnectionJoin -> {}
            ClientConnectionDisconnect -> {}
        }

        return Optional.empty()
    }
}