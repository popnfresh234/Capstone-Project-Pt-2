package com.dmtaiwan.alexander.iloveyoubike.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alexander on 10/19/2015.
 */
public class Station implements Parcelable {

    public Station() {

    }

    private int id;
    private String sna;
    private String snaen;
    private String sarea;
    private String sareaen;
    private double lat;
    private double lng;
    private long mday;

    //Bike data
    private int sbi; //bikes available
    private int bemp; //spaces available

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSna() {
        return sna;
    }

    public void setSna(String sna) {
        this.sna = sna;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getSnaen() {
        return snaen;
    }

    public void setSnaen(String snaen) {
        this.snaen = snaen;
    }

    public long getMday() {
        return mday;
    }

    public void setMday(long mday) {
        this.mday = mday;
    }

    public String getSarea() {
        return sarea;
    }

    public void setSarea(String sarea) {
        this.sarea = sarea;
    }

    public String getSareaen() {
        return sareaen;
    }

    public void setSareaen(String sareaen) {
        this.sareaen = sareaen;
    }

    public int getSbi() {
        return sbi;
    }

    public void setSbi(int sbi) {
        this.sbi = sbi;
    }

    public int getBemp() {
        return bemp;
    }

    public void setBemp(int bemp) {
        this.bemp = bemp;
    }


    protected Station(Parcel in) {
        id = in.readInt();
        sna = in.readString();
        snaen = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        mday = in.readLong();
        sarea = in.readString();
        sareaen = in.readString();
        sbi = in.readInt();
        bemp = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(sna);
        dest.writeString(snaen);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeLong(mday);
        dest.writeString(sarea);
        dest.writeString(sareaen);
        dest.writeInt(sbi);
        dest.writeInt(bemp);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>() {
        @Override
        public Station createFromParcel(Parcel in) {
            return new Station(in);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };
}