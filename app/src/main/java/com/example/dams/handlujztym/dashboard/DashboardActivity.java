package com.example.dams.handlujztym.dashboard;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.dams.handlujztym.R;
import com.example.dams.handlujztym.dashboard.pager.DashboardPagerAdapter;

public class DashboardActivity extends AppCompatActivity {

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // setup view pager adapter
        DashboardPagerAdapter pagerAdapter = new DashboardPagerAdapter(getSupportFragmentManager());

        // setup view pager
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount());

        // setup tab layout
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            tabLayout.getTabAt(i).setText(pagerAdapter.getTitle(i));
        }
    }
}