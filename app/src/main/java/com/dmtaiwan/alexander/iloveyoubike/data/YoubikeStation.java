package com.dmtaiwan.alexander.iloveyoubike.data;

/**
 * Created by Alexander on 7/28/2015.
 */
public class YoubikeStation {

    private int stationId;
    private String stationNameChinese;
    private String stationDistrictChinese;
    private String stationNameEnglish;
    private String stationDistrictEnglish;
    private double stationLat;
    private double stationLong;
    private int bikesAvailable;
    private int spacesAvailable;

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getStationNameChinese() {
        return stationNameChinese;
    }

    public void setStationNameChinese(String stationNameChinese) {
        this.stationNameChinese = stationNameChinese;
    }

    public String getStationDistrictChinese() {
        return stationDistrictChinese;
    }

    public void setStationDistrictChinese(String stationDistrictChinese) {
        this.stationDistrictChinese = stationDistrictChinese;
    }

    public String getStationDistrictEnglish() {
        return stationDistrictEnglish;
    }

    public void setStationDistrictEnglish(String stationDistrictEnglish) {
        this.stationDistrictEnglish = stationDistrictEnglish;
    }

    public String getStationNameEnglish() {
        return stationNameEnglish;
    }

    public void setStationNameEnglish(String stationNameEnglish) {
        this.stationNameEnglish = stationNameEnglish;
    }

    public double getStationLat() {
        return stationLat;
    }

    public void setStationLat(double stationLat) {
        this.stationLat = stationLat;
    }

    public double getStationLong() {
        return stationLong;
    }

    public void setStationLong(double stationLong) {
        this.stationLong = stationLong;
    }

    public int getBikesAvailable() {
        return bikesAvailable;
    }

    public void setBikesAvailable(int bikesAvailable) {
        this.bikesAvailable = bikesAvailable;
    }

    public int getSpacesAvailable() {
        return spacesAvailable;
    }

    public void setSpacesAvailable(int spacesAvailable) {
        this.spacesAvailable = spacesAvailable;
    }


}
