package com.dawndevelop.event

import java.util.*

enum class EventType {
    ClientConnectionJoin,
    ClientConnectionDisconnect,
    ChangeBlockBreak,
    ChangeBlockPlace,
    ChangeBlockModify,
    ChangeBlockGrow,
    ChangeBlockDecay
}