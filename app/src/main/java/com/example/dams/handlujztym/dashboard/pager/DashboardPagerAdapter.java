package com.example.dams.handlujztym.dashboard.pager;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.dams.handlujztym.R;
import com.example.dams.handlujztym.dashboard.pager.pages.DashboardMapFragment;
import com.example.dams.handlujztym.dashboard.pager.pages.DashboardSundaysFragment;

public class DashboardPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 2;

    private static final int PAGE_SUNDAYS = 0;
    private static final int PAGE_MAP = 1;

    public DashboardPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return position == PAGE_SUNDAYS ? DashboardSundaysFragment.newInstance() : DashboardMapFragment.newInstance();
    }

    @StringRes
    public int getTitle(int position) {
        return position == PAGE_SUNDAYS ? R.string.dashboard_sundays_title : R.string.dashboard_map_title;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}