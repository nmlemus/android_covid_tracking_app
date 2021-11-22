package com.goblob.covid.dagger;

import com.goblob.covid.app.CovidApp;
import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by edel on 4/3/18.
 */

@Singleton
@Component(modules={DataModule.class})
public interface DataComponent {

    void inject(CovidApp covidApplication);

    void inject(Injectable injectable);
}
