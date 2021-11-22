package com.goblob.covid.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.goblob.covid.app.CovidApp;
import com.goblob.covid.data.dao.model.Bluetooth;
import com.goblob.covid.data.dao.model.CorregimientosPA;
import com.goblob.covid.data.dao.model.DailyReportAll;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapViewModel extends ViewModel {
    public final String TAG = MapViewModel.class.getSimpleName();

    public MapViewModel() {
    }

    public LiveData<List<ParseObject>> getDailyReport() {
        final MutableLiveData<List<ParseObject>> data = new MutableLiveData<>();

        final List<ParseObject> locations = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("DailyReportAll");
        query.setLimit(500);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                locations.addAll(objects);
                data.setValue(locations);
            }
        });

        return data;
    }

    public LiveData<List<ParseObject>> getDailyReportUSA() {
        final MutableLiveData<List<ParseObject>> data = new MutableLiveData<>();

        final List<ParseObject> locations = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("DailyReportUSA");
        query.setLimit(500);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                locations.addAll(objects);
                data.setValue(locations);
            }
        });

        return data;
    }

    public LiveData<List<DailyReportAll>> getDailyReportAll() {
        final MutableLiveData<List<DailyReportAll>> data = new MutableLiveData<>();

        ParseQuery<DailyReportAll> query = ParseQuery.getQuery("DailyReportAll");
        query.orderByAscending("country");
        query.setLimit(500);
        query.findInBackground(new FindCallback<DailyReportAll>() {
            @Override
            public void done(List<DailyReportAll> objects, ParseException e) {
                data.setValue(objects);
            }
        });
        return data;
    }

    public LiveData<DailyReportAll> getDailyReport(String countryCode) {
        final MutableLiveData<DailyReportAll> data = new MutableLiveData<>();

        ParseQuery<DailyReportAll> query = ParseQuery.getQuery("DailyReportAll");
        query.whereEqualTo("country_code", countryCode);
        query.orderByAscending("country");
        query.setLimit(500);
        query.findInBackground(new FindCallback<DailyReportAll>() {
            @Override
            public void done(List<DailyReportAll> objects, ParseException e) {
                if(objects != null && objects.size() > 0) {
                    data.setValue(objects.get(0));
                } else {
                    data.setValue(null);
                }
            }
        });
        return data;
    }

    public LiveData<List<CorregimientosPA>> getDailyReportPA() {
        final MutableLiveData<List<CorregimientosPA>> data = new MutableLiveData<>();

        final List<CorregimientosPA> locations = new ArrayList<>();
        ParseQuery<CorregimientosPA> query = ParseQuery.getQuery("Corregimientos_pa");
        query.setLimit(2000);
        query.findInBackground(new FindCallback<CorregimientosPA>() {
            @Override
            public void done(List<CorregimientosPA> objects, ParseException e) {
                locations.addAll(objects);
                data.setValue(locations);
            }
        });

        return data;
    }

    public LiveData<List<ParseObject>> getCases() {
        final MutableLiveData<List<ParseObject>> data = new MutableLiveData<>();

        Ion.with(CovidApp.getInstance())
                .load("http://covid19cmap.com/covid19/covid19confirmedcases.php")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {

                        JsonArray confirmedA = result.getAsJsonArray("confirmed");

                        final List<ParseObject> cases = new ArrayList<>();

                        for (int i = 0; i < confirmedA.size(); i++) {
                            double lat = confirmedA.get(i).getAsJsonObject().get("lat").getAsDouble();
                            double lng = confirmedA.get(i).getAsJsonObject().get("lng").getAsDouble();
                            int confirmed = confirmedA.get(i).getAsJsonObject().get("confirmed").getAsInt();

                            String region = confirmedA.get(i).getAsJsonObject().get("region").getAsString();
                            String province = confirmedA.get(i).getAsJsonObject().get("province").getAsString();
                            ParseObject object = new ParseObject("Covid");
                            object.put("confirmed", confirmed);
                            object.put("region", region);
                            object.put("province", province);
                            object.put("location", new ParseGeoPoint(lat, lng));
                            cases.add(object);
                        }

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Covid");

                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                cases.addAll(objects);
                                data.setValue(cases);
                            }
                        });
                    }
                });

        return data;
    }

    public LiveData<List<Bluetooth>> getTrace(ParseUser user, Date time) {
        final MutableLiveData<List<Bluetooth>> data = new MutableLiveData<>();

        ParseQuery<Bluetooth> query = ParseQuery.getQuery("Bluetooth");
        query.whereEqualTo("device1", user.getUsername());

        // query.whereEqualTo("createdAt", "");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Date start = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, 1);

        Date end = calendar.getTime();

        /*ParseQuery<Bluetooth> query1 = query;

        query.whereEqualTo("device1", user.getUsername());
        query1.whereEqualTo("device2", user.getUsername());

        List<ParseQuery<Bluetooth>> queries = new ArrayList<ParseQuery<Bluetooth>>();
        queries.add(query);
        queries.add(query1);

        ParseQuery<Bluetooth> mainQuery = ParseQuery.or(queries);*/
        query.whereGreaterThanOrEqualTo("createdAt", start);
        query.whereLessThanOrEqualTo("createdAt", end);
        query.setLimit(1000000);
        query.orderByAscending("createdAt");

        query.findInBackground(new FindCallback<Bluetooth>() {
            @Override
            public void done(List<Bluetooth> objects, ParseException e) {
                data.setValue(objects);
            }
        });

        return data;
    }

    public LiveData<List<ParseObject>> getIntersections(ParseUser user) {
        final MutableLiveData<List<ParseObject>> data = new MutableLiveData<>();

        final List<ParseObject> locations = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Bluetooth");
        query.whereEqualTo("user", user);
        // query.whereEqualTo("createdAt", "");
        query.orderByAscending("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                locations.addAll(objects);
                data.setValue(locations);
            }
        });

        return data;
    }
}