package com.dmtaiwan.alexander.iloveyoubike.Presenters;

import android.content.Context;

import com.dmtaiwan.alexander.iloveyoubike.Models.MainInteractor;
import com.dmtaiwan.alexander.iloveyoubike.Models.MainInteractorImpl;
import com.dmtaiwan.alexander.iloveyoubike.Models.Station;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.DistanceComparator;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;
import com.dmtaiwan.alexander.iloveyoubike.Views.MainView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Alexander on 10/19/2015.
 */
public class MainPresenterImpl implements MainPresenter, MainInteractorImpl.MainInteractorListener {
    private static final String LOG_TAG = MainPresenterImpl.class.getSimpleName();

    private MainView mView;
    private Context mContext;
    private MainInteractor mInteractor;


    public MainPresenterImpl(MainView mainView, Context context) {
        this.mView = mainView;
        this.mContext = context;
        this.mInteractor = new MainInteractorImpl(this, context);
    }


    @Override
    public void requestData() {
        mInteractor.fetchData();
    }


    @Override
    public void onResult(String json) {
        mView.hideProgress();
        if (json != null) {
            List<Station> stationList = parseJson(json);
            mView.onDataReturned(stationList);
        }else{
            mView.onDataReturned(null);
        }
    }

    @Override
    public void showProgress() {
        mView.showProgress();
    }

    private List<Station> parseJson(String json) {
        List<Station> pojoStations = new ArrayList<Station>();
        try {

            JSONObject jsonString = new JSONObject(json);
            JSONObject result = jsonString.getJSONObject("result");
            JSONArray results = result.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject stationObject = results.getJSONObject(i);
                Station station = new Station();
                int id = stationObject.getInt("_id");
                String name = stationObject.getString("sna");
                String engName = stationObject.getString("snaen");
                String district = stationObject.getString("sarea");
                String engDistrict = stationObject.getString("sareaen");
                double stationLat = stationObject.getDouble("lat");
                double stationLong = stationObject.getDouble("lng");
                long time = stationObject.getLong("mday");
                station.setId(id);
                station.setSna(name);
                station.setSnaen(engName);
                station.setSarea(district);
                station.setSareaen(engDistrict);
                station.setLat(stationLat);
                station.setLng(stationLong);
                station.setMday(time);
                pojoStations.add(station);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            pojoStations = null;
        }


        if (pojoStations != null) {
            //Sort by distance
            DistanceComparator comparator = new DistanceComparator(Utilities.getUserLocation(mContext));
            Collections.sort(pojoStations, comparator);
        }


        return pojoStations;
    }
}
