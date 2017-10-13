package com.dawndevelop.blockmonitor.api;

import com.dawndevelop.blockmonitor.BlockMonitor;
import lombok.Builder;
import lombok.Data;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.Date;
import java.sql.Timestamp;

@Builder
@Data
public class Event {
    final long Id = BlockMonitor.snowflake.next();

    EventType EventType;

    String EventInformation;

    String PlayerInformation;

    String BlockInformation;

    String ItemStackInformation;

    Location<World> WorldLocation;

    final Timestamp Timestamp = new Timestamp(System.currentTimeMillis());
}
