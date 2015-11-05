package com.dmtaiwan.alexander.iloveyoubike.Models;

/**
 * Created by Alexander on 10/19/2015.
 */
public class HttpResponse {
    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    private int responseCode;
    private String response;
}
