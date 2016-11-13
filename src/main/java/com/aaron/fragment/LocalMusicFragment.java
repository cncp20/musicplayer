package com.aaron.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.aaron.adapter.LocalMusicAdapter;
import com.aaron.bean.MusicInfo;
import com.aaron.musicplayer.MainActivity;
import com.aaron.musicplayer.R;
import com.aaron.utils.MusicUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/16.
 */
public class LocalMusicFragment extends Fragment{
    private MainActivity mActivity;
    private List<MusicInfo> mMusics;
    public ListView mLvLocal;
    private LocalMusicAdapter mAdapter;
    private MainActivityCallBack mainActivityCallBack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(mActivity, R.layout.fragment_local_music,null);
        mLvLocal = (ListView) view.findViewById(R.id.lv_local_musics);
        mLvLocal.setAdapter(mAdapter);
        mLvLocal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mainActivityCallBack.onMusicPosition(i);
            }
        });
        return view;
    }
    private void initData() {
        mMusics = new ArrayList<>();
        mMusics = MusicUtil.getMusicList(mActivity);
        mAdapter = new LocalMusicAdapter(mActivity,mMusics);
    }


    public interface MainActivityCallBack{
        void onMusicPosition(int i);
    }

    public void setMainActivityCallBack(MainActivityCallBack callBack){
        mainActivityCallBack = callBack;
    }

}
