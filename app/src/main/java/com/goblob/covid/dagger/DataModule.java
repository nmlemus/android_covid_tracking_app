package com.goblob.covid.dagger;

import android.app.Application;


import com.goblob.covid.data.dao.factory.DAOFactory;
import com.goblob.covid.data.dao.factory.DAOFactoryType;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by edel on 4/3/18.
 */

@Module
public class DataModule {
    Application application;

    public DataModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    public DAOFactory provideDAOFactory() {
        return DAOFactory.getDAOFactory(DAOFactoryType.PARSE);
    }
}
