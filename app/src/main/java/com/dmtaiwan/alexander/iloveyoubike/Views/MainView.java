package com.dmtaiwan.alexander.iloveyoubike.Views;


import com.dmtaiwan.alexander.iloveyoubike.Models.Station;

import java.util.List;

/**
 * Created by Alexander on 10/19/2015.
 */
public interface MainView {

    void onDataReturned(List<Station> stationList);

    void showProgress();

    void hideProgress();
}
