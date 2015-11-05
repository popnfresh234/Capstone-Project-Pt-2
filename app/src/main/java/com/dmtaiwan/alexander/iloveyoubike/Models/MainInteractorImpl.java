package com.dmtaiwan.alexander.iloveyoubike.Models;


import android.content.Context;

import com.dmtaiwan.alexander.iloveyoubike.Service.YoubikeService;
import com.dmtaiwan.alexander.iloveyoubike.Utilities.Utilities;

import java.net.HttpURLConnection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Alexander on 10/19/2015.
 */
public class MainInteractorImpl implements MainInteractor {

    private MainInteractorListener mListener;
    private Context mContext;

    public MainInteractorImpl(MainInteractorListener listener, Context context) {
        this.mListener = listener;
        mContext = context;
    }

    @Override
    public void fetchData() {
        //First check if data is available, if so set adapter
        if (Utilities.doesFileExist(mContext)) {
            String json = Utilities.readFromFile(mContext);
            mListener.onResult(json);
        }

        if (Utilities.isNetworkAvailable(mContext)) {
            //Show progress when we query network
            mListener.showProgress();
            YoubikeService youbikeService = new YoubikeService();
            Observable<HttpResponse> httpResultObservable = youbikeService.loadStations();
            Action1<HttpResponse> subscriber = new Action1<HttpResponse>() {
                @Override
                public void call(HttpResponse httpResponse) {
                    String response = processData(httpResponse);
                    mListener.onResult(response);
                }
            };

            httpResultObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);
        } else {
            //If not return null so we can set empty view
            mListener.onResult(null);
        }
    }


    private String processData(HttpResponse httpResponse) {
        int responseCode = httpResponse.getResponseCode();

        //If http response was good, get the json string
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Utilities.writeToFile(httpResponse.getResponse(), mContext);
            return httpResponse.getResponse();
        }

        //If the http response was not good, try to read from file
        else if (Utilities.doesFileExist(mContext)) {
            return Utilities.readFromFile(mContext);
        }

        //If we cant find the file, return null to set empty view
        else {
            return null;
        }
    }


    public interface MainInteractorListener {
        void onResult(String json);

        void showProgress();
    }
}
