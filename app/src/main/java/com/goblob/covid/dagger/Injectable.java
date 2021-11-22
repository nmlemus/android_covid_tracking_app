package com.goblob.covid.dagger;


import com.goblob.covid.data.dao.NotificationDao;
import com.goblob.covid.data.dao.factory.DAOFactory;

import javax.inject.Inject;

public class Injectable {

    @Inject
    protected DAOFactory daoFactory = new DAOFactory() {
        @Override
        public NotificationDao getNotificationDao() {
            return super.getNotificationDao();
        }
    };

    private static Injectable instance;

    public static Injectable get(){
        if (instance == null){
            instance = new Injectable();
        }

        return instance;
    }

    public DAOFactory getDaoFactory() {
        return daoFactory;
    }
}
