package com.amict.blockmonitor.api;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public class JsonItemStackSnapshotConverter {
    public static Optional<String> toJson(ItemStackSnapshot itemStackSnapshot){
        DataContainer itemStackSnapshotDataContainer = itemStackSnapshot.toContainer();
        DataFormat dataFormat = DataFormats.JSON;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            dataFormat.writeTo(outputStream, itemStackSnapshotDataContainer);
            return Optional.of(new String(outputStream.toByteArray(), Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }

    }

    public static Optional<ItemStackSnapshot> fromJson(String json){
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());
        DataFormat dataFormat = DataFormats.JSON;
        try {
            DataContainer dataContainer = dataFormat.readFrom(inputStream);
            return Sponge.getDataManager().deserialize(ItemStackSnapshot.class, dataContainer);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}

