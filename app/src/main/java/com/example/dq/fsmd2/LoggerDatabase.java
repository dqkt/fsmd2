package com.example.dq.fsmd2;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Item.class, MonitorData.class}, version = 2)
public abstract class LoggerDatabase extends RoomDatabase {
    private static LoggerDatabase INSTANCE;

    public static LoggerDatabase getLoggerDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), LoggerDatabase.class, "logger-database")
                    .fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }

    public static void destroyLoggerDatabase() {
        INSTANCE = null;
    }

    public abstract Item.ItemDao itemModel();
    public abstract MonitorData.MonitorDataDao monitorDataModel();
}
