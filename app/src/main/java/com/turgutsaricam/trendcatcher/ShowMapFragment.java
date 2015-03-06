package com.turgutsaricam.trendcatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;


public class ShowMapFragment extends Fragment {
    View v;

    MapView mapView;
    GoogleMap map;
    CameraUpdate cameraUpdate;

    MapUtilsView mapUtilsView;
    TextView tvTweetCount;
    EditText etTweetLimit;

    FetchTweetsTask fetchTweetsTask;
    List<twitter4j.Status> mAllTweets = new ArrayList<twitter4j.Status>();
    private int TWEET_COUNT_LIMIT = 500;

    private List<HashMap<String, String>> mTweets = new ArrayList<HashMap<String,String>>();

    CommunicatorShowMapFragment comm;
    DecimalFormat df;

    List<Polyline> allMapRectangles = new ArrayList<Polyline>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        comm = (CommunicatorShowMapFragment) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        df = new DecimalFormat("#0.0");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.show_map_fragment, container, false);

        // Get the mapview from xml layout file
        mapView = (MapView) v.findViewById(R.id.mapView);
        tvTweetCount = (TextView) v.findViewById(R.id.tvTweetCount);

        mapUtilsView = (MapUtilsView) v.findViewById(R.id.mapUtilsView);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpMap(savedInstanceState);
        loadAllTweets();
    }

    private void setUpMap(Bundle savedInstanceState) {
        // Get the map from mapview and initialize
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Updates the location and zoom of the MapView
        cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(39.8851, 32.7819), 5f);
        map.animateCamera(cameraUpdate);

        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
                TextView tvName = (TextView) v.findViewById(R.id.tvName);
                TextView tvTweet = (TextView) v.findViewById(R.id.tvTweet);

                tvName.setText(marker.getTitle());
                tvTweet.setText(marker.getSnippet());

                return v;
            }
        });
    }

    private void loadAllTweets() {
        mAllTweets.addAll(comm.getAllTweets());
        LatLng latLngHolder;
        GeoLocation geoLocationHolder;
        for(Status status : mAllTweets) {
            geoLocationHolder = status.getGeoLocation();
            latLngHolder = new LatLng(
                    geoLocationHolder.getLatitude(),
                    geoLocationHolder.getLongitude()
            );

            map.addMarker(new MarkerOptions()
                    .position(latLngHolder)
                    .title(status.getUser().getName() + " @" + status.getUser().getScreenName())
                    .snippet(status.getText())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_small))
            );
        }

        tvTweetCount.setText(String.valueOf(mAllTweets.size()));
    }

    private void getTweetsFromStream(LatLng leftBottom, LatLng rightTop, LatLng rightBottom, LatLng leftTop) {
        mapUtilsView.setActive(false);

        Polyline mapRectangle = map.addPolyline(new PolylineOptions()
                        .add(leftBottom)
                        .add(rightBottom)
                        .add(rightTop)
                        .add(leftTop)
                        .add(leftBottom)
                        .color(Color.rgb(3, 169, 244))
                        .width(2f)
        );

        allMapRectangles.add(mapRectangle);
        switchMenuIcon(false, true);

        // Send stream starting time to main activity
        // to use it later on showing tweets list
        comm.setStreamStartTime(Calendar.getInstance().getTimeInMillis());

        makeToast("Getting tweets...");

//        LatLng leftBottom = new LatLng(39.915557, 32.841728);
//        LatLng rightTop = new LatLng(39.932539, 32.874387);

        cancelFetchTweetsTask();
        fetchTweetsTask = new FetchTweetsTask(leftBottom, rightTop, rightBottom, leftTop);
        fetchTweetsTask.execute();

    }

    public void makeToast(CharSequence message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public void logIt(String text) {
        Log.e("ShowTweetsFragment", text);
    }

    private class FetchTweetsTask extends AsyncTask<Void, twitter4j.Status, Void> {

        double[][] boundary;
        ConfigurationBuilder cb;
        StatusListener statusListener;

        TwitterStream ts;
        LatLng leftBottom, rightTop, rightBottom, leftTop;

        public FetchTweetsTask(LatLng leftBottom, LatLng rightTop, LatLng rightBottom, LatLng leftTop) {
            boundary = new double[][] {
                    {leftBottom.longitude, leftBottom.latitude},
                    {rightTop.longitude, rightTop.latitude}
            };
            this.leftBottom = leftBottom;
            this.rightTop = rightTop;
            this.rightBottom = rightBottom;
            this.leftTop = leftTop;
        }

        @Override
        protected void onPreExecute() {
            // Get authentication
            cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(MyConstants.CONSUMER_KEY)
                    .setOAuthConsumerSecret(MyConstants.CONSUMER_SECRET)
                    .setOAuthAccessToken(MyConstants.ACCESS_TOKEN)
                    .setOAuthAccessTokenSecret(MyConstants.ACCESS_TOKEN_SECRET);

            // Create a status listener which includes methods to be used after a tweet is retrieved
            statusListener = new StatusListener() {
                @Override
                public void onStatus(twitter4j.Status status) {
                    logIt(status.getUser().getName() + " -> " + status.getText());
                    GeoLocation location = status.getGeoLocation();
                    if(location != null) {
                        publishProgress(status);
                    } else {
                        logIt("Location: NULL");
                    }
                }

                @Override
                public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

                }

                @Override
                public void onTrackLimitationNotice(int i) {

                }

                @Override
                public void onScrubGeo(long l, long l2) {

                }

                @Override
                public void onStallWarning(StallWarning stallWarning) {

                }

                @Override
                public void onException(Exception e) {
                    logIt("StatusListener Exception " + e.toString());
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Build the stream and add status listener
            ts = new TwitterStreamFactory(cb.build()).getInstance();
            ts.addListener(statusListener);

            // Set up filtering
            FilterQuery fq = new FilterQuery();
            fq.locations(boundary);

            ts.filter(fq);

            return null;
        }

        @Override
        protected void onProgressUpdate(twitter4j.Status... values) {
            logIt("...");
            logIt("New status published");

            if(mAllTweets.size() < TWEET_COUNT_LIMIT) {
                twitter4j.Status currentStatus = values[0];

                if(isTweetInBoundaries(currentStatus)) {
                    // Add status to the list holding all of the tweets
                    mAllTweets.add(currentStatus);

                    GeoLocation location = currentStatus.getGeoLocation();
                    LatLng latLng = new LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    );

                    Marker mMarker = map.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(currentStatus.getUser().getName() + " @" + currentStatus.getUser().getScreenName())
                                    .snippet(currentStatus.getText())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_small))
                    );
                    mMarker.showInfoWindow();
                    tvTweetCount.setText(String.valueOf(mAllTweets.size()));
                }
            } else {
                stopStream();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            logIt("onPostExecute");
        }

        public void stopStream() {
            if(ts != null) {
                ts.shutdown();
                ts.clearListeners();
                ts = null;
                comm.setStreamEndTime(Calendar.getInstance().getTimeInMillis());
                makeToast("Stream has been stopped.");
            }
        }

        public boolean isTweetInBoundaries(twitter4j.Status status) {
            double statusLat = status.getGeoLocation().getLatitude();
            double statusLng = status.getGeoLocation().getLongitude();

            Point pLeftTop = map.getProjection().toScreenLocation(leftTop);
            Point pRightBottom = map.getProjection().toScreenLocation(rightBottom);

            Rect boundaryRect = new Rect(pLeftTop.x, pLeftTop.y, pRightBottom.x, pRightBottom.y);
            Point pTweet = map.getProjection().toScreenLocation(new LatLng(statusLat, statusLng));
            if(boundaryRect.contains(pTweet.x, pTweet.y)) {
                return true;
            }

            return false;
        }
    }

    public void getScreenCoords(float leftBottomX, float leftBottomY, float rightTopX, float rightTopY) {
//        logIt("leftBottom: " + leftBottomX + " - " + leftBottomY);
//        logIt("rightTop: " + rightTopX + " - " + rightTopY);
        final LatLng leftBottom = map.getProjection().fromScreenLocation(new Point((int) leftBottomX, (int) leftBottomY));
        final LatLng rightTop = map.getProjection().fromScreenLocation(new Point((int) rightTopX, (int) rightTopY));

        final LatLng rightBottom = map.getProjection().fromScreenLocation(new Point((int) rightTopX, (int) leftBottomY));
        final LatLng leftTop = map.getProjection().fromScreenLocation(new Point((int) leftBottomX, (int) rightTopY));

        float[] results = new float[1];
        Location.distanceBetween(leftBottom.latitude, leftBottom.longitude, rightTop.latitude, rightTop.longitude, results);

        String distanceInKM = df.format((double) (results[0]/1000));
        logIt("Distance is " + distanceInKM + " km");
        logIt("LeftBottom: " + leftBottom.toString());
        logIt("RightTop: " + rightTop.toString());

        String messageAddition = "";
        if(fetchTweetsTask != null && fetchTweetsTask.getStatus() == AsyncTask.Status.RUNNING) {
            messageAddition = " Current stream will be cancelled.";
        }

        View v = getActivity().getLayoutInflater().inflate(R.layout.get_tweets_dialog, null);
        etTweetLimit = (EditText) v.findViewById(R.id.etTweetCountLimit);
        etTweetLimit.setText(String.valueOf(TWEET_COUNT_LIMIT));

        TextView tvMessage = (TextView) v.findViewById(R.id.tvMessage);
        tvMessage.setText("Get tweet stream from selected area?" + messageAddition);

        new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton("Get Tweets", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String number = etTweetLimit.getText().toString();
                        if(!number.isEmpty()) {
                            TWEET_COUNT_LIMIT = Integer.parseInt(number);
                        } else {
                            makeToast("Tweet limit input is empty. Limit is set to 500.");
                            TWEET_COUNT_LIMIT = 500;
                        }
                        getTweetsFromStream(leftBottom, rightTop, rightBottom, leftTop);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    MenuItem miSwitch = null;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tweet_map, menu);
        miSwitch = menu.findItem(R.id.menuSwitchMapUtils);
        switchMenuIcon(mapUtilsView.isActive(), false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuListTweets:
                comm.setAllTweets(mAllTweets);
                DialogShowTweets dialog = new DialogShowTweets();
                dialog.show(getActivity().getSupportFragmentManager(), "DialogShowTweets");
                break;
            case R.id.menuSwitchMapUtils:
                boolean activeStatus = mapUtilsView.isActive();
                mapUtilsView.setActive(!activeStatus);
                switchMenuIcon(!activeStatus, true);
                break;
            case R.id.menuClearTweets:
                map.clear();
                mAllTweets.clear();
                comm.setAllTweets(mAllTweets);
                tvTweetCount.setText(String.valueOf(mAllTweets.size()));
                break;
            case R.id.menuStopStream:
                if(fetchTweetsTask != null) fetchTweetsTask.stopStream();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchMenuIcon(boolean activeStatus, boolean makeToast) {
        miSwitch.setIcon(activeStatus ? R.drawable.ic_android_white : R.drawable.ic_draw);
        if(makeToast) makeToast(activeStatus ? "Drawing mode" : "Pan mode");
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void cancelFetchTweetsTask() {
        if(fetchTweetsTask != null) {
            fetchTweetsTask.stopStream();
            fetchTweetsTask.cancel(true);
        }
    }

    @Override
    public void onDestroy() {
        cancelFetchTweetsTask();

        super.onDestroy();
        mapView.onDestroy();
        comm.setAllTweets(mAllTweets);
    }

    public interface CommunicatorShowMapFragment {
        public void setAllTweets(List<twitter4j.Status> tweetList);
        public List<twitter4j.Status> getAllTweets();
        public void setStreamStartTime(long epochInMillis);
        public void setStreamEndTime(long epochInMillis);
    }

    public List<Status> getAllTweets() {
        return mAllTweets;
    }
}
