package com.aaron.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Administrator on 2016/8/16.
 */
public class MainVPAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    public MainVPAdapter(FragmentManager fm) {
        super(fm);
    }
    public MainVPAdapter(FragmentManager fm, List fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
