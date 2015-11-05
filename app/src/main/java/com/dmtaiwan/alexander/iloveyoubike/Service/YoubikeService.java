package com.dmtaiwan.alexander.iloveyoubike.Service;


import com.dmtaiwan.alexander.iloveyoubike.Models.HttpResponse;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.net.HttpURLConnection;
import java.net.URL;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Alexander on 10/19/2015.
 */
public class YoubikeService {

    public Observable<HttpResponse> loadStations() {
        Observable<HttpResponse> stationObservable = Observable.create(new Observable.OnSubscribe<HttpResponse>() {
            @Override
            public void call(Subscriber<? super HttpResponse> subscriber) {
                String apiUrl = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=ddb80380-f1b3-4f8e-8016-7ed9cba571d5";
                HttpResponse httpResponse = new HttpResponse();
                int resultCode;
                try {
                    URL url = new URL(apiUrl);
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = client.newCall(request).execute();
                    resultCode = response.code();
                    httpResponse.setResponseCode(resultCode);
                    if (resultCode == HttpURLConnection.HTTP_OK) {
                        httpResponse.setResponse(response.body().string());
                        subscriber.onNext(httpResponse);
                    } else {
                        subscriber.onNext(httpResponse);
                    }
                } catch (Exception e) {
                    httpResponse.setResponseCode(0);
                    httpResponse.setResponse(e.toString());
                    subscriber.onNext(httpResponse);
                }
            }
        });
        return stationObservable;
    }
}
