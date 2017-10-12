package com.dawnmcdevelop.blockmonitor.api;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataFormats;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Created by johnfg10 on 12/06/2017.
 */
public class DataContainerHelper {

    public static Optional<DataContainer> getDataContainerFromString(String dataContainerString) {
        try {
            InputStream inputStream = new ByteArrayInputStream(dataContainerString.getBytes());
            DataFormat dataFormat = DataFormats.JSON;
            DataContainer dataContainer = dataFormat.readFrom(inputStream);
            return Optional.of(dataContainer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
