package com.turgutsaricam.trendcatcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import twitter4j.Status;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ShowMapFragment.CommunicatorShowMapFragment,
        DialogShowTweets.CommunicatorDialogShowTweets,
        MapUtilsView.CommunicatorMapUtilsView,
        ProfilePhotosFragment.CommunicatorProfilePhotosFragment
{

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    Toolbar mToolbar;
    TwitterLoginButton twLoginButton;
    FrameLayout mFragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(MyConstants.TWITTER_KEY, MyConstants.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        mNavigationDrawerFragment.disableDrawer();

        twLoginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        mFragmentContainer = (FrameLayout) findViewById(R.id.container);

//        twLoginButton.setVisibility(View.VISIBLE);
//        mFragmentContainer.setVisibility(View.GONE);

        twLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                makeToast("Login Successful");
                setVisibilities(true);

                mNavigationDrawerFragment.setFirstItemSelected();
            }

            @Override
            public void failure(TwitterException e) {
                makeToast("Login Failed");
                setVisibilities(false);
            }
        });

        setVisibilities(true);

    }

    private void setVisibilities(boolean loggedIn) {
        if(!loggedIn) {
            twLoginButton.setVisibility(View.VISIBLE);
            mFragmentContainer.setVisibility(View.GONE);
            mNavigationDrawerFragment.disableDrawer();

        } else {
            twLoginButton.setVisibility(View.GONE);
            mFragmentContainer.setVisibility(View.VISIBLE);
            mNavigationDrawerFragment.enableDrawer();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result to the login button.
//        twLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        String tag = "";
        Bundle args;
        switch (position) {
            case 0:
                fragment = new ShowMapFragment();
                tag = "ShowMapFragment";
                break;
            case 1:
                fragment = new ProfilePhotosFragment();
                tag = "ProfilePhotosFragment";
                args = new Bundle();
                args.putBoolean(ProfilePhotosFragment.LOAD_TWEET_PHOTOS, false);
                fragment.setArguments(args);
                break;
            case 2:
                fragment = new ProfilePhotosFragment();
                tag = "ProfilePhotosFragment";
                args = new Bundle();
                args.putBoolean(ProfilePhotosFragment.LOAD_TWEET_PHOTOS, true);
                fragment.setArguments(args);
                break;
        }

        if(fragment != null) {
            mTitle = mNavigationDrawerFragment.itemNames[position];
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, tag)
                    .commit();
        } else {
            if(!tag.isEmpty()) makeToast(tag);
        }
    }

    private void logOut() {
        Twitter.getSessionManager().clearActiveSession();
        setVisibilities(false);
        makeToast("You have logged out successfully");
    }

    public void makeToast(CharSequence message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
//            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    List<ShowMapFragment.StreamObject> mAllStreamObjects = new ArrayList<ShowMapFragment.StreamObject>();
    @Override
    public void setAllStreamObjects(List<ShowMapFragment.StreamObject> tweetList) {
        mAllStreamObjects.clear();
        mAllStreamObjects.addAll(tweetList);
    }

    @Override
    public List<ShowMapFragment.StreamObject> getAllStreamObjects() {
        FragmentManager fm = getSupportFragmentManager();
        ShowMapFragment fragment = (ShowMapFragment) fm.findFragmentByTag("ShowMapFragment");
        if(fragment != null) {
            if(fragment.getAllStreamObjects().size() > mAllStreamObjects.size()) {
                mAllStreamObjects.clear();
                mAllStreamObjects.addAll(fragment.getAllStreamObjects());
            }
        }

        return mAllStreamObjects;
    }

    long streamStartEpoch = 0;
    long streamEndEpoch = 0;
    @Override
    public long setStreamStartTime() {
        streamStartEpoch = Calendar.getInstance().getTimeInMillis();
        streamEndEpoch = 0;
        return streamStartEpoch;
    }

    @Override
    public long setStreamEndTime() {
        streamEndEpoch = Calendar.getInstance().getTimeInMillis();
        return streamEndEpoch;
    }

    public String calculateTimeDifference(long startEpochInMillis) {
        Calendar calendar = Calendar.getInstance();
        long endTime = streamEndEpoch != 0 ? streamEndEpoch : calendar.getTimeInMillis();
        long millisDiff = endTime - startEpochInMillis;

        SimpleDateFormat dateFormat;
        String addText = "";
        if(millisDiff < 60 * 1000L) {
            dateFormat = new SimpleDateFormat("ss", Locale.US);
            addText = " sec";
        } else if(millisDiff < 60 * 60 * 1000L) {
            dateFormat = new SimpleDateFormat("mm:ss", Locale.US);
        } else {
            dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        }

        return dateFormat.format(new Date(millisDiff)) + addText;
    }

    @Override
    public List<ShowMapFragment.StreamObject> requestStreamObjects() {
        return getAllStreamObjects();
    }

    @Override
    public String requestTimeDifference() {
        return calculateTimeDifference(streamStartEpoch);
    }

    @Override
    public void onScreenCoordsTaken(float leftBottomX, float leftBottomY, float rightTopX, float rightTopY) {
        FragmentManager fm = getSupportFragmentManager();
        ShowMapFragment fragment = (ShowMapFragment) fm.findFragmentByTag("ShowMapFragment");

        if(fragment != null) {
            fragment.getScreenCoords(leftBottomX, leftBottomY, rightTopX, rightTopY);
        }
    }

    @Override
    public List<ShowMapFragment.StreamObject> getStreamObjects() {
        return mAllStreamObjects;
    }
}
