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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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


    public RecyclerAdapterStation(Context context, StationAdapterOnClickHandler clickHandler, View emptyView) {
        mContext = context;
        mClickHandler = clickHandler;
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

        String time = formatTime(mCursor.getString(StationListFragment.COL_LAST_UPDATED));
        holder.distance.setText(time);
    }

    private String formatTime(String string) {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        String timeString = "";
        try {
            Date date = format.parse(string);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            String hourString = String.valueOf(hour);
            String minuteString = String.valueOf(minute);
            timeString = hourString + ":" + minuteString;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timeString;
    }


    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @InjectView(R.id.text_view_station_list_station_name)
        TextView stationName;
        @InjectView(R.id.text_view_station_list_distance)
        TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int stationId = mCursor.getInt(StationListFragment.COL_STATION_ID);
            mClickHandler.onClick(stationId, this);
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
