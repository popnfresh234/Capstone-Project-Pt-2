package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.R;
import com.dmtaiwan.alexander.iloveyoubike.StationListFragment;
import com.google.gson.Gson;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Alexander on 7/27/2015.
 */
public class RecyclerAdapterStation extends RecyclerView.Adapter<RecyclerAdapterStation.ViewHolder> {

    private static final String LOG_TAG = RecyclerAdapterStation.class.getSimpleName();


    private Cursor mCursor;
    final private Context mContext;
    final private StationAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;
    final String mLanguage;
    private Location mUserLocation;


    public RecyclerAdapterStation(Context context, StationAdapterOnClickHandler clickHandler, View emptyView) {
        mContext = context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mLanguage = preferences.getString(mContext.getString(R.string.pref_key_language), mContext.getString(R.string.pref_language_english));
        String jsonString = preferences.getString(Utilities.SHARED_PREFS_LOCATION_KEY, "");
        if (!jsonString.equals("")) {
            try {
                Gson gson = new Gson();
                mUserLocation = gson.fromJson(jsonString, Location.class);
            } catch (Exception e) {
                e.printStackTrace();
                mUserLocation = null;
            }
        }else{
            mUserLocation = null;
        }
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

        if (mLanguage.equals(mContext.getString(R.string.pref_language_english))) {
            holder.stationName.setText(mCursor.getString(StationListFragment.COL_STATION_NAME_EN));
        }else{
            holder.stationName.setText(mCursor.getString(StationListFragment.COL_STATION_NAME_ZH));
        }

        ViewCompat.setTransitionName(holder.stationName, mContext.getResources().getString(R.string.transitoin_station_name_text)+position);
        ViewCompat.setTransitionName(holder.stationStatus, mContext.getResources().getString(R.string.transition_status_iamge_view)+position);

        String time = Utilities.formatTime(mCursor.getString(StationListFragment.COL_LAST_UPDATED));
        holder.time.setText(time);

        //calculate the distasnce from the user's last known location
        double stationLat = mCursor.getDouble(StationListFragment.COL_STATION_LAT);
        double stationLong = mCursor.getDouble(StationListFragment.COL_STATION_LONG);

        if (mUserLocation != null) {
            float distance = Utilities.calculateDistance(stationLat, stationLong, mUserLocation);
            holder.distance.setText(Utilities.formatDistance(distance));
        }

//        Set the icon based on bike availability
//        int bikesAvailable = mCursor.getInt(StationListFragment.COL_BIKES_AVAILABLE);
//        int spacesAvailable = mCursor.getInt(StationListFragment.COL_SPACES_AVAILABLE);
//        Log.i(LOG_TAG, String.valueOf(bikesAvailable));
//        Log.i(LOG_TAG, String.valueOf(spacesAvailable));
//        if (bikesAvailable > 0 && spacesAvailable > 0) {
//            holder.stationStatus.setImageResource(R.drawable.green48x48);
//        }
//        else if (spacesAvailable == 0) {
//            holder.stationStatus.setImageResource(R.drawable.yellow48x48);
//        }
//        else if (bikesAvailable == 0) {
//            holder.stationStatus.setImageResource(R.drawable.red48x48);
//        }
        holder.stationStatus.setImageResource(Utilities.getStatusIconDrawable(mCursor));

    }




    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @InjectView(R.id.image_view_station_list_status)
        public ImageView stationStatus;
        @InjectView(R.id.text_view_station_list_station_name)
        public TextView stationName;
        @InjectView(R.id.text_view_station_list_time)
        TextView time;
        @InjectView(R.id.text_view_station_list_distance) TextView distance;

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
        Log.i(LOG_TAG, "swapCursor");
        Log.i(LOG_TAG, String.valueOf(getItemCount()));
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }


}
