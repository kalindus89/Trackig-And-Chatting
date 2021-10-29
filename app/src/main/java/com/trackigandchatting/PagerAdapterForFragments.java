package com.trackigandchatting;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.trackigandchatting.fragments.CallFragment;
import com.trackigandchatting.fragments.ChatFragment;
import com.trackigandchatting.fragments.StatusFragment;

public class PagerAdapterForFragments extends FragmentPagerAdapter {

    int tabCount;

    public PagerAdapterForFragments(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        tabCount =behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return  new ChatFragment();
            case 1:
                return  new StatusFragment();
            case 2:
                return  new CallFragment();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
