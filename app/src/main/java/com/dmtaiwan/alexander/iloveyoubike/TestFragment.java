package com.dmtaiwan.alexander.iloveyoubike;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Alexander on 9/12/2015.
 */
public class TestFragment extends Fragment {
    private String mTitle;
    private int mPage;

        @Bind(R.id.text_view_test)
        TextView mTestTextView;


    public static TestFragment newInstance(int page, String title) {
        TestFragment testFragment = new TestFragment();
        Bundle args = new Bundle();
        args.putInt("pageInt", page);
        args.putString("title", title);
        testFragment.setArguments(args);
        return testFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt("pageInt", 0);
        mTitle = getArguments().getString("title");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test, container, false);
        ButterKnife.bind(this, rootView);
        mTestTextView.setText(String.valueOf(mPage)+ " " + mTitle);
        return rootView;
    }
}
