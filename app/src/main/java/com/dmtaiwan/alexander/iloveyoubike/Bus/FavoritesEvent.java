package com.dmtaiwan.alexander.iloveyoubike.Bus;

import java.util.ArrayList;

/**
 * Created by Alexander on 10/25/2015.
 */
public class FavoritesEvent {
    public ArrayList<String> getFavoritesArray() {
        return favoritesArray;
    }

    public void setFavoritesArray(ArrayList<String> favoritesArray) {
        this.favoritesArray = favoritesArray;
    }

    private ArrayList<String> favoritesArray;
}
