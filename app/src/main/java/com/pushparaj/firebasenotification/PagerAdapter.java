package com.pushparaj.firebasenotification;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class PagerAdapter extends FragmentPagerAdapter {
    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0 :
                return new MapFragmentClass();
            case 1 :
                return new RequestsFragment();
            case 2 :
                return new FreindsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 1:return "SEARCH";
            case 0:return "MAP";
            case 2:return "FRIENDS";
            default:return "";
        }

    }
}
