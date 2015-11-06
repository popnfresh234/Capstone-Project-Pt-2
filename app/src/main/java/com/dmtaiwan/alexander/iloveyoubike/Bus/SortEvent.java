package com.dmtaiwan.alexander.iloveyoubike.Bus;

/**
 * Created by lenovo on 11/6/2015.
 */
public class SortEvent {

    public SortEvent(int sortCode) {
        mSortCode = sortCode;
    }

    private int mSortCode;

    public int getSortCode() {
        return mSortCode;
    }
}
