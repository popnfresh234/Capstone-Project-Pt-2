package com.dmtaiwan.alexander.iloveyoubike.Utilities;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dmtaiwan.alexander.iloveyoubike.MainActivity;
import com.dmtaiwan.alexander.iloveyoubike.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Alexander on 7/27/2015.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    private Context mContext;
    public RecyclerAdapter(Context context) {
        mContext = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_main, parent, false);
        return new ViewHolder(itemView, mContext);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int imageId;
        switch (position) {
            case 0:
                imageId = R.drawable.ic_favorite_black_48dp;
                break;
            case 1:
                imageId = R.drawable.ic_my_location_black_48dp;
                break;
            case 2:
                imageId = R.drawable.ic_sort_black_48dp;
                break;
            case 3:
                imageId = R.drawable.ic_map_black_48dp;
                break;
            default:imageId = 0;
        }
        holder.imageView.setImageResource(imageId);
        holder.textView.setText(mContext.getResources().getStringArray(R.array.main_list_items)[position]);
        Log.i("TEST", holder.textView.getText().toString());
    }

    @Override
    public int getItemCount() {
        return mContext.getResources().getStringArray(R.array.main_list_items).length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private Context mContext;
        @InjectView(R.id.image_view_icon_list_view_main)
        ImageView imageView;

        @InjectView(R.id.text_view_label_list_viewJ_main)
        TextView textView;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
            mContext = context;
        }

        @Override
        public void onClick(View v) {
            MainActivity mainActivity = (MainActivity) mContext;
            mainActivity.onListItemClick(getAdapterPosition());
        }
    }
}
