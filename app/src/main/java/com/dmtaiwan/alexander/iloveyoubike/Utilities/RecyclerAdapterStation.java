package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.R;
import com.dmtaiwan.alexander.iloveyoubike.StationListFragment;
import com.dmtaiwan.alexander.iloveyoubike.data.YoubikeStation;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Alexander on 7/27/2015.
 */
public class RecyclerAdapterStation extends RecyclerView.Adapter<RecyclerAdapterStation.ViewHolder> {

    private ArrayList<YoubikeStation> mYoubikeStations;
    private Cursor mCursor;
    final private Context mContext;
    final private StationAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;


    public RecyclerAdapterStation(Context context, StationAdapterOnClickHandler dh, View emptyView) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;

    }

    public static interface StationAdapterOnClickHandler {
        void onClick(int stationId, RecyclerAdapterStation.ViewHolder vh);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_station_list, parent, false);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.stationName.setText(mCursor.getString(StationListFragment.COL_STATION_NAME_EN));
        holder.distance.setText(String.valueOf(mCursor.getDouble(StationListFragment.COL_STATION_LAT)));
    }


    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.text_view_station_list_station_name)
        TextView stationName;
        @InjectView(R.id.text_view_station_list_distance)
        TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0? View.VISIBLE : View.GONE);
    }

}
