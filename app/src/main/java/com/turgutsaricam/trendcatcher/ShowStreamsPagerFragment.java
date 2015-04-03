package com.turgutsaricam.trendcatcher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.util.Calendar;

/**
 * Created by Turgut on 03.04.2015.
 */
public class ShowStreamsPagerFragment extends Fragment {
    View v;
    ViewPager viewPager;
    PagerSlidingTabStrip viewPagerTabStrip;
    MyPagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            restorePage = savedInstanceState.getInt("restorePage");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.show_streams_pager_fragment, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPagerTabStrip = (PagerSlidingTabStrip) v.findViewById(R.id.viewPagerTabStrip);

        setViews();

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void setViews() {
        mPagerAdapter = new MyPagerAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(mPagerAdapter);

        viewPagerTabStrip.setShouldExpand(true);
        viewPagerTabStrip.setViewPager(viewPager);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPagerTabStrip.notifyDataSetChanged();
                restorePage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    int restorePage = 0;
    private void refreshViewPager() {
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setCurrentItem(restorePage);

        viewPagerTabStrip.notifyDataSetChanged();
    }

    class MyPagerAdapter extends SmartFragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new ShowStreamsFragment();
            Bundle args = new Bundle();
            int loadedDay = -1;
            switch (i) {
                case 0:
                    loadedDay = Calendar.MONDAY;
                    break;
                case 1:
                    loadedDay = Calendar.TUESDAY;
                    break;
                case 2:
                    loadedDay = Calendar.WEDNESDAY;
                    break;
                case 3:
                    loadedDay = Calendar.THURSDAY;
                    break;
                case 4:
                    loadedDay = Calendar.FRIDAY;
                    break;
                case 5:
                    loadedDay = Calendar.SATURDAY;
                    break;
                case 6:
                    loadedDay = Calendar.SUNDAY;
                    break;
            }
            args.putInt("loaded_day", loadedDay);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 7;
        }

        CharSequence[] titles = new CharSequence[] {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }

    /*
    * http://guides.codepath.com/android/ViewPager-with-FragmentPagerAdapter#dynamic-viewpager-fragments
    * Extension of FragmentStatePagerAdapter which intelligently caches
    * all active fragments and manages the fragment life cycles.
    * Usage involves extending from SmartFragmentStatePagerAdapter as you would any other PagerAdapter.
    */
    public abstract class SmartFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
        // Sparse array to keep track of registered fragments in memory
        private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public SmartFragmentStatePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Register the fragment when the item is instantiated
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        // Unregister when the item is inactive
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        // Returns the fragment for the position (if instantiated)
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("restorePage", restorePage);
    }
}
