package com.amict.blockmonitor.api;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by johnfg10 on 12/06/2017.
 */
public class DataContainerHelper {

    public static Optional<ItemStackSnapshot> getItemStackSnapshotFromDataContainer(DataContainer dataContainer){
        final Optional<DataBuilder<ItemStackSnapshot>> dataBuilderOptional = Sponge.getDataManager().getBuilder(ItemStackSnapshot.class);
        if (dataBuilderOptional.isPresent()){
            DataBuilder<ItemStackSnapshot> itemStackSnapshotDataBuilder = dataBuilderOptional.get();
            Optional<DataView> dataViewOptional = dataContainer.getView(DataQuery.of("item"));
            if (dataViewOptional.isPresent()){
                return itemStackSnapshotDataBuilder.build(dataViewOptional.get());
            }
        }
        return Optional.empty();
    }

    public static Optional<BlockSnapshot> getBlockSnapshotFromDataContainer(DataContainer dataContainer){
        final Optional<DataBuilder<BlockSnapshot>> dataBuilderOptional = Sponge.getDataManager().getBuilder(BlockSnapshot.class);
        if (dataBuilderOptional.isPresent()){
            DataBuilder<BlockSnapshot> blockSnapshotDataBuilder = dataBuilderOptional.get();
            //System.out.println(dataContainer.getKeys(true));
            Optional<DataView> dataViewOptional = dataContainer.getView(DataQuery.of("block"));
            if (dataViewOptional.isPresent()){
                return blockSnapshotDataBuilder.build(dataViewOptional.get());
            }
        }
        return Optional.empty();
    }

    public static Optional<User> getUserFromDataContainer(DataContainer dataContainer){
        final Optional<DataBuilder<User>> dataBuilderOptional = Sponge.getDataManager().getBuilder(User.class);
        if (dataBuilderOptional.isPresent()){
            DataBuilder<User> userDataBuilder = dataBuilderOptional.get();
            Optional<DataView> dataViewOptional = dataContainer.getView(DataQuery.of("player"));
            if (dataViewOptional.isPresent()){
                return userDataBuilder.build(dataViewOptional.get());
            }
        }
        return Optional.empty();
    }

    public static Optional<Entity> getEntityFromDataContainer(DataContainer dataContainer){
        final Optional<DataBuilder<Entity>> dataBuilderOptional = Sponge.getDataManager().getBuilder(Entity.class);
        if (dataBuilderOptional.isPresent()){
            DataBuilder<Entity> entityDataBuilder = dataBuilderOptional.get();
            Optional<DataView> dataViewOptional = dataContainer.getView(DataQuery.of("entity"));
            if (dataViewOptional.isPresent()){
                return entityDataBuilder.build(dataViewOptional.get());
            }
        }
        return Optional.empty();
    }

    public ByteArrayOutputStream toJsonFormat(ItemStackSnapshot itemStack){
        DataContainer playerDataContainer = itemStack.toContainer();
        DataFormat dataFormat = DataFormats.JSON;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            dataFormat.writeTo(outputStream, playerDataContainer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream;
    }
}
