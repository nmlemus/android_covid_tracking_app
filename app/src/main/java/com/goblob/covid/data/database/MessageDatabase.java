package com.goblob.covid.data.database;

import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.goblob.covid.data.dao.NotificationDao;
import com.goblob.covid.data.dao.model.Notification;

/**
 * Created by root on 17/08/17.
 */

/*
exportSchema is a mandatory argument, its default value is set to true
but you would need to provide a folder to export the schema
More info here: https://stackoverflow.com/a/44645943/5552022
*/
@Database(entities = {Notification.class}, version = 1, exportSchema = false)
public abstract class MessageDatabase extends RoomDatabase {

    public abstract NotificationDao notificationDAO();

    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    @Override
    public void clearAllTables() {

    }
}
