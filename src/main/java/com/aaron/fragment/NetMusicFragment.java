package com.aaron.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaron.musicplayer.MainActivity;
import com.aaron.musicplayer.R;

/**
 * Created by Administrator on 2016/8/16.
 */
public class NetMusicFragment extends Fragment{
    private MainActivity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(mActivity, R.layout.fragment_net_music,null);
        return view;
    }
}
