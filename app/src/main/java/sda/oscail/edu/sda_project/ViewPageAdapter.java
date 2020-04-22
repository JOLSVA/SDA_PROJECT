package sda.oscail.edu.sda_project;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPageAdapter extends FragmentPagerAdapter {

    private Context context;

    ViewPageAdapter(FragmentManager fm, int behavior, Context nContext) {
        super(fm, behavior);
        context = nContext;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        Fragment fragment = new Fragment();

        //finds the tab position (note array starts at 0)
        position = position+1;

        //finds the fragment
        switch (position)
        {
            case 1:
                //code
                fragment = new Welcome();
                break;
            case 2:
                //code
                fragment = new Goal();
                break;
            case 3:
                //code
                fragment = new Day();
                break;
            case 4:
                //code
                fragment = new Statistics();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        position = position+1;

        CharSequence tabTitle = "";

        //finds the fragment
        switch (position)
        {
            case 1:
                //code
                tabTitle = "HOME";
                break;
            case 2:
                //code
                tabTitle = "GOAL";
                break;
            case 3:
                //code
                tabTitle = "DAY";
                break;
            case 4:
                //code
                tabTitle = "STATS";
                break;
        }

        return tabTitle;
    }
}
