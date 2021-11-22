package com.goblob.covid.data.dao.factory;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.goblob.covid.app.CovidApp;
import com.goblob.covid.data.dao.NotificationDao;
import com.goblob.covid.data.database.MessageDatabase;


public abstract class DAOFactory {
    private static DAOFactoryType currentUsedStorage;
    private MessageDatabase messageDatabase;

    public DAOFactory() {
        messageDatabase = Room.databaseBuilder(CovidApp.getInstance(), MessageDatabase.class, "covid-message.db")
                //.addMigrations(MIGRATION_1_2)
                /*.addMigrations(new Migration(8, 9) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase database) {
                        // database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS 'messageFts' USING FTS4('messageText', content='message')");
                        // database.execSQL("INSERT INTO messageFts(messageFts) VALUES ('rebuild')");
                    }
                })
                .fallbackToDestructiveMigration()*/
                .build();
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE message "
                    + " ADD COLUMN end_time TEXT");
            database.execSQL("ALTER TABLE message "
                    + " ADD COLUMN start_time TEXT");
        }
    };
    /**
     * @return the currentUsedStorage
     */
    public static DAOFactoryType getCurrentUsedStorage()
    {
        return currentUsedStorage;
    }

    public static DAOFactory getDAOFactory(DAOFactoryType factoryType)
    {
        switch (factoryType)
        {
            case PARSE :
                currentUsedStorage = DAOFactoryType.PARSE;
                return new ParseDAOFactory();
            default :
                return null;
        }
    }

    public NotificationDao getNotificationDao(){
        return messageDatabase.notificationDAO();
    }
}
