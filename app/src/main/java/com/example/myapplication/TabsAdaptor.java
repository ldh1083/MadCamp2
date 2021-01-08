package com.example.myapplication;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAdaptor extends FragmentPagerAdapter {

    private final Context mContext;

    public TabsAdaptor(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return PhoneNumberFragment.newInstance(position + 1);
            case 1:
                return RemainFragment.newInstance(position + 1);
            case 2:
                return GetContactDemoFragment.newInstance(position + 1);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}