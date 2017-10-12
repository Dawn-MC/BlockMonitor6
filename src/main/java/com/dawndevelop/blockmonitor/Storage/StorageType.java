package com.dawndevelop.blockmonitor.Storage;

import java.util.Optional;

/**
 * Created by johnfg10 on 04/06/2017.
 */
public enum StorageType {
    h2,
    mysql
    ;

    public boolean isFileBased(){
        switch (this){
            case h2:
                return true;
            case mysql:
                return false;
        }
        return true;
    }

    public Optional<IStorageHandler> GetStorageHandler(){
        switch (this){
            case mysql:
                return Optional.of(new MysqlHandler());
        }
        return Optional.empty();
    }
}
