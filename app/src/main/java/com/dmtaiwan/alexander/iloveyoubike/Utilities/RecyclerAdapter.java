package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.Bus.EventBus;
import com.dmtaiwan.alexander.iloveyoubike.Bus.RecyclerClickEvent;
import com.dmtaiwan.alexander.iloveyoubike.Models.Station;
import com.dmtaiwan.alexander.iloveyoubike.R;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by Alexander on 10/19/2015.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private static final String LOG_TAG = RecyclerAdapter.class.getSimpleName();
    private Context mContext;
    final private View mEmptyView;
    private List<Station> mStationList;

    public RecyclerAdapter(Context context, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_station_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String language = preferences.getString(mContext.getString(R.string.pref_key_language), mContext.getString(R.string.pref_language_english));
        //Get station
        Station station = mStationList.get(position);

        //Check preferred language and set stationName
        if (language.equals(mContext.getString(R.string.pref_language_english))) {
            holder.stationName.setText(station.getSnaen());
        } else {
            holder.stationName.setText(station.getSna());
        }

        //Set the updatedTime
        String time = Utilities.formatTime(String.valueOf(station.getMday()));
        holder.updatedTime.setText(time);

        //Set distance
        Location userLocation = Utilities.getUserLocation(mContext);
        if (userLocation != null) {
            float distance = Utilities.calculateDistance(station.getLat(), station.getLng(), userLocation);
            holder.distance.setText(Utilities.formatDistance(distance));
        }
    }

    @Override
    public int getItemCount() {
        if (mStationList != null) {
            return mStationList.size();
        } else {
            return 0;
        }

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        @Bind(R.id.text_view_station_list_station_name)
        public TextView stationName;
        @Bind(R.id.text_view_station_list_time)
        TextView updatedTime;
        @Bind(R.id.text_view_station_list_distance) TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Station station = mStationList.get(adapterPosition);
            RecyclerClickEvent recyclerClickEvent = new RecyclerClickEvent(station);
            EventBus.getInstance().post(recyclerClickEvent);
        }
    }

    public void sortData(int sortCode) {
        Utilities.setSortOrder(sortCode, mContext);
        sortList(sortCode);
        notifyDataSetChanged();
    }

    public void updateData(List<Station> stationList) {
        Log.i(LOG_TAG, "updateData");
        mStationList = stationList;
        int sortCode = Utilities.getSortOrder(mContext);
        sortList(sortCode);
        notifyDataSetChanged();
        mEmptyView.setVisibility(mStationList.size() == 0 ? View.VISIBLE : View.GONE);
    }

    public void setEmptyView() {
        mEmptyView.setVisibility(View.VISIBLE);
    }

    private void sortList(int sortCode) {
        if (mStationList != null) {
            if (sortCode == Utilities.SORT_PROXIMITY) {

                //Sort by distance
                DistanceComparator comparator = new DistanceComparator(Utilities.getUserLocation(mContext));
                Collections.sort(mStationList, comparator);

            } else {
                //Sort by default ID order
                IDComparator idComparator = new IDComparator();
                Collections.sort(mStationList, idComparator);
            }
        }
    }
}
