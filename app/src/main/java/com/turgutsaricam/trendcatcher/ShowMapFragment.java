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
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;


public class ShowMapFragment extends Fragment {
    View v;

    MapView mapView;
    GoogleMap map;
    CameraUpdate cameraUpdate;

    MapUtilsView mapUtilsView;
    TextView tvTweetCount, tvCurrentStreamTweetCount, tvDurationLimit;
    EditText etTweetLimit;
    Chronometer chronometer;

    FetchTweetsTask fetchTweetsTask;
    private int TWEET_COUNT_LIMIT = 500;

    CommunicatorShowMapFragment comm;
    DecimalFormat df;

    List<StreamObject> allStreamObjects = new ArrayList<StreamObject>();
    List<StatusObject> allStatusObjects = new ArrayList<StatusObject>();
    List<InfoMarker> allInfoMarkers = new ArrayList<InfoMarker>();

    final int COLOR_RED = Color.rgb(244, 67, 54);
    final int COLOR_BLUE = Color.rgb(3, 169, 244);

    long streamStartTime = 0l;
    long streamEndTime = 0l;

    long streamCount = 0;

    public static class StreamObject {
        private long id;
        private Polyline mapRectangle;
        private long startEpoch;
        private long endEpoch;
        private List<StatusObject> statusObjects = new ArrayList<StatusObject>();

        LatLng leftTop, leftBottom, rightBottom, rightTop;

        private int tweetLimit = 500;
        private long durationLimit = 0;
        String elapsedTime = "";
        String status = "";
        boolean manuallyEnded = false;

        InfoMarker infoMarker;
        
        public StreamObject(long id, Polyline mapRectangle, long startEpoch, int tweetLimit) {
            this.id = id;
            this.mapRectangle = mapRectangle;
            this.startEpoch = startEpoch;
            this.tweetLimit = tweetLimit;
        }

        public void setDurationLimit(long durationInMillis) {
            durationLimit = durationInMillis;
        }

        public void setStreamEnded(long endEpoch, GoogleMap map, boolean manuallyEnded, List<InfoMarker> allInfoMarkers) {
            this.endEpoch = endEpoch;
            elapsedTime = getTimeAsDate(false);
            this.manuallyEnded = manuallyEnded;
            // TODO
            String[] titleAndSnippet = getTitleAndSnippet();

            Marker mMarker = map.addMarker(new MarkerOptions()
                            .position(leftTop)
                            .title(titleAndSnippet[0])
                            .snippet(titleAndSnippet[1])
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_info))
            );
            mMarker.showInfoWindow();

            infoMarker = new InfoMarker(this, mMarker);
            allInfoMarkers.add(infoMarker);

            // Reset map rotation
            CameraPosition cameraPosition = new CameraPosition(map.getCameraPosition().target,
                    map.getCameraPosition().zoom, map.getCameraPosition().tilt, 0f);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            map.animateCamera(cameraUpdate);
        }
        
        public void setTweets(List<StatusObject> tweets) {
            this.statusObjects.clear();
            this.statusObjects.addAll(tweets);
        }

        public void addStatusObject(StatusObject statusObject) {
            statusObjects.add(statusObject);
        }
        
        public void setCorners(LatLng leftTop, LatLng leftBottom, LatLng rightBottom, LatLng rightTop) {
            this.leftTop = leftTop;
            this.leftBottom = leftBottom;
            this.rightBottom = rightBottom;
            this.rightTop = rightTop;
        }

        public void setInfoMarker(InfoMarker im) {
            this.infoMarker = im;
        }

        public long getId() { return id; }
        public Polyline getMapRectangle() { return mapRectangle; }
        public List<StatusObject> getStatusObjects() { return statusObjects; }
        public long getStartEpoch() { return startEpoch; }
        public long getEndEpoch() { return endEpoch; }
        public int getTweetLimit() { return tweetLimit; }
        public String getElapsedTime() { return elapsedTime; }
        public long getDurationLimit() { return durationLimit; }
        public InfoMarker getInfoMarker() { return infoMarker; }

        private String getTimeAsDate(boolean isDurationLimit) {
            long millisDiff;
            if(!isDurationLimit) {
                millisDiff = endEpoch - startEpoch;
            } else {
                millisDiff = durationLimit;
                if(durationLimit == 0) {
                    return "infinite";
                }
            }

            SimpleDateFormat dateFormat;
            String addText = "";
            if (millisDiff < 60 * 1000L) {
                dateFormat = new SimpleDateFormat("ss", Locale.US);
                addText = " sec";
            } else if (millisDiff < 60 * 60 * 1000L) {
                dateFormat = new SimpleDateFormat("mm:ss", Locale.US);
            } else {
                dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
            }

            return dateFormat.format(new Date(millisDiff)) + addText;
        }

        public String getDurationAsChronometerFormat() {
            String s = "";
            SimpleDateFormat dateFormat;
            if(durationLimit > 0) {
                dateFormat = new SimpleDateFormat("mm:ss", Locale.US);
                s = dateFormat.format(new Date(durationLimit));
            } else {
                s = "\u221E";
            }

            return s;
        }

        public String[] getTitleAndSnippet() {
            String title = statusObjects.size() + " tweets in " + elapsedTime;

            String snippet = "Tweet Limit: " + (tweetLimit > 0 ? tweetLimit : "infinite")
                    + "\nDuration limit: " + getTimeAsDate(true);
            if(manuallyEnded) {
                snippet += "\nStatus: Interrupted";
            } else {
                snippet += "\nStatus: Finished";
            }

            return new String[] { title, snippet };
        }

    }

    public static class StatusObject {
        Status status;
        Marker marker;
        long tweetId;

        public StatusObject(Status status, Marker marker) {
            this.status = status;
            this.marker = marker;
            this.tweetId = status.getId();
        }

        public Marker getMarker() {
            return marker;
        }

        public Status getStatus() {
            return status;
        }

        public long getTweetId() {
            return tweetId;
        }

        public void setMarker(Marker marker) {
            this.marker = marker;
        }
    }

    public static class InfoMarker {
        private StreamObject streamObject;
        private Marker marker;

        public InfoMarker(StreamObject streamObject, Marker marker) {
            this.marker = marker;
            this.streamObject = streamObject;
        }

        public Marker getMarker() {
            return marker;
        }

        public StreamObject getStreamObject() {
            return streamObject;
        }
    }

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
//        v.setKeepScreenOn(true);

        // Get the mapview from xml layout file
        mapView = (MapView) v.findViewById(R.id.mapView);
        tvTweetCount = (TextView) v.findViewById(R.id.tvTweetCount);
        tvCurrentStreamTweetCount = (TextView) v.findViewById(R.id.tvCurrentStreamTweetCount);
        tvDurationLimit = (TextView) v.findViewById(R.id.tvDurationLimit);

        mapUtilsView = (MapUtilsView) v.findViewById(R.id.mapUtilsView);
        chronometer = (Chronometer) v.findViewById(R.id.chronometer);
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
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Updates the location and zoom of the MapView
        cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(39.8851, 32.7819), 4f);
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

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                for(StatusObject so : allStatusObjects) {
                    if(so.getMarker().equals(marker)) {
                        dialogSingleTweet(so);
                    }
                }

                for(InfoMarker im : allInfoMarkers) {
                    if(im.marker.equals(marker)) {
                        dialogEndedStreamOptions(im.getStreamObject());
                    }
                }
            }
        });
    }

    private void loadAllTweets() {
        allStreamObjects.addAll(comm.getAllStreamObjects());
        for(StreamObject so : allStreamObjects) {
            for (StatusObject statusObject : so.getStatusObjects()) {
                Status status = statusObject.getStatus();

                GeoLocation location = status.getGeoLocation();
                LatLng latLng = new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );

                Marker mMarker = map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(status.getUser().getName() + " @" + status.getUser().getScreenName())
                            .snippet(status.getText())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_small))
                );

                statusObject.setMarker(mMarker);
                allStatusObjects.add(statusObject);
            }

            String[] titleAndSnippet = so.getTitleAndSnippet();
            Marker mMarker = map.addMarker(new MarkerOptions()
                            .position(so.leftTop)
                            .title(titleAndSnippet[0])
                            .snippet(titleAndSnippet[1])
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_info2))
            );

            InfoMarker infoMarker = new InfoMarker(so, mMarker);
            so.setInfoMarker(infoMarker);
            allInfoMarkers.add(infoMarker);

            so.mapRectangle = map.addPolyline(new PolylineOptions()
                            .add(so.leftBottom)
                            .add(so.rightBottom)
                            .add(so.rightTop)
                            .add(so.leftTop)
                            .add(so.leftBottom)
                            .color(COLOR_BLUE)
                            .width(2f)
            );
        }

        tvTweetCount.setText(String.valueOf(calculateTweetCount()));
    }

    private void getTweetsFromStream(LatLng leftBottom, LatLng rightTop, LatLng rightBottom, LatLng leftTop,
                                     double distanceInKM, LatLng centerLocation, int tweetLimit, final long duration) {
        mapUtilsView.setActive(false);

        Polyline mapRectangle = map.addPolyline(new PolylineOptions()
                        .add(leftBottom)
                        .add(rightBottom)
                        .add(rightTop)
                        .add(leftTop)
                        .add(leftBottom)
                        .color(COLOR_BLUE)
                        .width(2f)
        );

        switchMenuIcon(false, true);

        makeToast("Getting tweets...");

        // Cancel if there is a task running
        cancelFetchTweetsTask(true);

        // Send stream starting time to main activity
        // to use it later on showing tweets list
        streamStartTime = comm.setStreamStartTime();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        StreamObject mStreamObject = new StreamObject(++streamCount, mapRectangle, streamStartTime, tweetLimit);
        mStreamObject.setCorners(leftTop, leftBottom, rightBottom, rightTop);
        mStreamObject.setDurationLimit(duration);

        // Set the chronometer tick listener so that current stream is shut down
        // when the duration is reached.
        if(duration != 0) {
            chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if((SystemClock.elapsedRealtime() - chronometer.getBase()) >= duration) {
                        cancelFetchTweetsTask(false);
                    }
                }
            });
        } else {
            chronometer.setOnChronometerTickListener(null);
        }

        tvDurationLimit.setText("/" + mStreamObject.getDurationAsChronometerFormat());
        tvDurationLimit.setVisibility(View.VISIBLE);

        allStreamObjects.add(mStreamObject);

        // Set currentStreamTweetCount text view visible
        tvCurrentStreamTweetCount.setVisibility(View.VISIBLE);
        tvCurrentStreamTweetCount.setText("0");

        // Set tweet count limit text view
        String per = mStreamObject.getTweetLimit() > 0 ? String.valueOf(mStreamObject.getTweetLimit()) : "\u221E";
        tvCurrentStreamTweetCount.setText("0/" + per);

//        LatLng leftBottom = new LatLng(39.915557, 32.841728);
//        LatLng rightTop = new LatLng(39.932539, 32.874387);

//        // First, search tweets in that area
//        Query query = new Query();
//        query.setGeoCode(new GeoLocation(centerLocation.latitude, centerLocation.longitude), distanceInKM, Query.KILOMETERS);
//        new SearchTweetsTask(query, mStreamObject, per).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        // Next, get the stream
        fetchTweetsTask = new FetchTweetsTask(mStreamObject, per);
        fetchTweetsTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

    }

    public void makeToast(CharSequence message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public void logIt(String text) {
        Log.e("ShowTweetsFragment", text);
    }

    private class SearchTweetsTask extends AsyncTask<Void, Status, Void> {

        Query mQuery = null;
        
        StreamObject streamObject;
        LatLng leftTop, rightBottom;

        String tweetLimit;

        protected SearchTweetsTask(Query mQuery, StreamObject streamObject, String tweetLimit) {
            this.mQuery = mQuery;
            this.leftTop = streamObject.leftTop;
            this.rightBottom = streamObject.rightBottom;

            this.streamObject = streamObject;
            this.tweetLimit = tweetLimit;

            logIt("Query is: " + mQuery.toString());
        }

        @Override
        protected Void doInBackground(Void... params) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(MyConstants.CONSUMER_KEY)
                    .setOAuthConsumerSecret(MyConstants.CONSUMER_SECRET)
                    .setOAuthAccessToken(MyConstants.ACCESS_TOKEN)
                    .setOAuthAccessTokenSecret(MyConstants.ACCESS_TOKEN_SECRET);

            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();

            try {
                QueryResult queryResult = twitter.search(mQuery);
                for(twitter4j.Status status : queryResult.getTweets()) {
                    if(streamObject.getStatusObjects().size() >= streamObject.getTweetLimit()) {
                        break;
                    }
                    publishProgress(status);
                }
            } catch (TwitterException e) {
                logIt(e.toString());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(twitter4j.Status... values) {
            if(isTweetInBoundaries(values[0], leftTop, rightBottom)) {
                handleTakenStatus(values[0], streamObject, tweetLimit);
            }
        }
    }

    private class FetchTweetsTask extends AsyncTask<Void, twitter4j.Status, Void> {

        double[][] boundary;
        ConfigurationBuilder cb;
        StatusListener statusListener;

        TwitterStream ts;
        LatLng leftBottom, rightTop, rightBottom, leftTop;
        
        StreamObject streamObject;
        String tweetLimit;
        
        public FetchTweetsTask(StreamObject streamObject, String tweetLimit) {
            this.streamObject = streamObject;
            
            this.leftBottom = streamObject.leftBottom;
            this.rightTop = streamObject.rightTop;
            this.rightBottom = streamObject.rightBottom;
            this.leftTop = streamObject.leftTop;
            boundary = new double[][] {
                    {leftBottom.longitude, leftBottom.latitude},
                    {rightTop.longitude, rightTop.latitude}
            };

            this.tweetLimit = tweetLimit;

        }

        @Override
        protected void onPreExecute() {
            // Set map rectangle color
            streamObject.getMapRectangle().setColor(COLOR_RED);

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
//                    logIt(status.getUser().getName() + " -> " + status.getText());
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
            logIt("New status published");
            twitter4j.Status currentStatus = values[0];

            if(streamObject.getTweetLimit() > 0) {
                if (streamObject.getStatusObjects().size() < streamObject.getTweetLimit()) {
                    if (isTweetInBoundaries(currentStatus, leftTop, rightBottom)) {
                        handleTakenStatus(currentStatus, streamObject, tweetLimit);
                    }
                }

                if(streamObject.getStatusObjects().size() >= streamObject.getTweetLimit()) {
                    stopStream(false);
                }

            } else {
                if (isTweetInBoundaries(currentStatus, leftTop, rightBottom)) {
                    handleTakenStatus(currentStatus, streamObject, tweetLimit);
                }
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            logIt("onPostExecute");
        }

        public void stopStream(boolean manuallyEnded) {
            if(ts != null) {
                ts.shutdown();
                ts.clearListeners();
                ts = null;

                // Change the color of map rectangle
                streamObject.getMapRectangle().setColor(COLOR_BLUE);

                // Set stream end time
                streamEndTime = comm.setStreamEndTime();
                streamObject.setStreamEnded(streamEndTime, map, manuallyEnded, allInfoMarkers);
                chronometer.stop();

                // Set current stream count and duration limit text view insivible
                tvCurrentStreamTweetCount.setVisibility(View.GONE);
                tvDurationLimit.setVisibility(View.GONE);
            }
        }

        public boolean isStreamActive() {
            return ts != null;
        }

    }

    public boolean isTweetInBoundaries(twitter4j.Status status, LatLng leftTop, LatLng rightBottom) {
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

    private void handleTakenStatus(Status currentStatus, StreamObject streamObject, String per) {
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

        // Create new TweetMarker and add it to the list
        StatusObject statusObject = new StatusObject(currentStatus, mMarker);
        allStatusObjects.add(statusObject);

        // Add status to the list holding all of the tweets
        streamObject.addStatusObject(statusObject);

        tvTweetCount.setText(String.valueOf(calculateTweetCount()));

        tvCurrentStreamTweetCount.setText(streamObject.getStatusObjects().size() + "/" + per);
        logIt("   Status added.");
    }

    public int calculateTweetCount() {
        int count = 0;
        for(StreamObject so : allStreamObjects) {
            count += so.getStatusObjects().size();
        }
        return count;
    }

    public void getScreenCoords(float leftBottomX, float leftBottomY, float rightTopX, float rightTopY) {
//        logIt("leftBottom: " + leftBottomX + " - " + leftBottomY);
//        logIt("rightTop: " + rightTopX + " - " + rightTopY);
        final LatLng rightBottom = map.getProjection().fromScreenLocation(new Point((int) rightTopX, (int) leftBottomY));
        final LatLng leftTop = map.getProjection().fromScreenLocation(new Point((int) leftBottomX, (int) rightTopY));

        // Find a radius that encapsulates the selected area
        final LatLng leftBottom = map.getProjection().fromScreenLocation(new Point((int) leftBottomX, (int) leftBottomY));
        final LatLng rightTop = map.getProjection().fromScreenLocation(new Point((int) rightTopX, (int) rightTopY));

        float[] results = new float[1];
        Location.distanceBetween(leftBottom.latitude, leftBottom.longitude, rightTop.latitude, rightTop.longitude, results);

//        final String distanceInKM = df.format((double) (results[0]/1000));
        final double distanceInKM = (double) (results[0]/1000);
        logIt("Distance is " + distanceInKM + " km");

        // Find the center geolocation of the selected area
        float centerX = (leftBottomX + rightTopX) / 2;
        float centerY = (leftBottomY + rightTopY) / 2;

        final LatLng centerLocation = map.getProjection().fromScreenLocation(new Point((int) centerX, (int) centerY));
        logIt("Center Location: " + centerLocation.toString());

        // Show a dialog to the user for confirmation and tweet limit arrangement
        String messageAddition = "";
        if(fetchTweetsTask != null && fetchTweetsTask.isStreamActive()) {
            messageAddition = " Current stream will be cancelled.";
        }

        View v = getActivity().getLayoutInflater().inflate(R.layout.get_tweets_dialog, null);
        etTweetLimit = (EditText) v.findViewById(R.id.etTweetCountLimit);
        etTweetLimit.setText(String.valueOf(TWEET_COUNT_LIMIT));

        final EditText etDurationLimit = (EditText) v.findViewById(R.id.etDurationLimit);

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

                        number = etDurationLimit.getText().toString();
                        long duration = 0;
                        if(!number.isEmpty()) {
                            duration = Long.parseLong(number) * 1000l; // Multiply by 1000 to convert it to milliseconds
                        }

                        getTweetsFromStream(leftBottom, rightTop, rightBottom, leftTop, distanceInKM, centerLocation, TWEET_COUNT_LIMIT, duration);
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

    private void dialogSingleTweet(final StatusObject statusObject) {
        final LinearLayout llHolder = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.progress, null);

        final ProgressBar progressBar = (ProgressBar) llHolder.findViewById(R.id.progressBar);

        TweetUtils.loadTweet(statusObject.getTweetId(), new LoadCallback<Tweet>() {
            @Override
            public void success(Tweet tweet) {
                llHolder.removeView(progressBar);
                llHolder.addView(new TweetView(getActivity(), tweet));
            }

            @Override
            public void failure(com.twitter.sdk.android.core.TwitterException e) {
                makeToast("Failed to load");
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(llHolder);
        builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final List<StatusObject> allStatusObjectsReversed = getAllStatusObjectsReversed();
        final int index = allStatusObjectsReversed.indexOf(statusObject);
        if(index != (allStatusObjectsReversed.size() - 1)) {
            builder.setPositiveButton("Next (" + (index+2) + "/" + allStatusObjectsReversed.size() + ")", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    dialogSingleTweet(allStatusObjectsReversed.get(index + 1));
                }
            });
        }

        if(index != 0) {
            builder.setNegativeButton("Previous (" + (index) + "/" + allStatusObjectsReversed.size() + ")", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    dialogSingleTweet(allStatusObjectsReversed.get(index - 1));
                }
            });
        }

        builder.show();
    }

    // TODO
    private void dialogEndedStreamOptions(final StreamObject streamObject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Delete this stream?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                streamObject.getInfoMarker().getMarker().remove();
                streamObject.getMapRectangle().remove();

                List<StatusObject> statuses = streamObject.getStatusObjects();
                for(StatusObject so : statuses) {
                    so.getMarker().remove();
                }

                allStatusObjects.removeAll(statuses);
                allInfoMarkers.remove(streamObject.getInfoMarker());
                allStreamObjects.remove(streamObject);

                tvTweetCount.setText(String.valueOf(calculateTweetCount()));

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private List<StatusObject> getAllStatusObjectsReversed() {
        List<StatusObject> copyOfStatusObjects = new ArrayList<>(allStatusObjects);
        Collections.reverse(copyOfStatusObjects);

        return copyOfStatusObjects;
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
                comm.setAllStreamObjects(allStreamObjects);
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
                allStreamObjects.clear();
                comm.setAllStreamObjects(allStreamObjects);
                tvTweetCount.setText(String.valueOf(calculateTweetCount()));
                break;
            case R.id.menuStopStream:
                if(fetchTweetsTask != null) fetchTweetsTask.stopStream(true);
                break;
            case R.id.menuKeepScreenOn:
                item.setChecked(!item.isChecked());

                if(item.isChecked()) {
                    v.setKeepScreenOn(true);
                } else {
                    v.setKeepScreenOn(false);
                }

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

    private void cancelFetchTweetsTask(boolean manuallyEnded) {
        if(fetchTweetsTask != null) {
            fetchTweetsTask.stopStream(manuallyEnded);
            fetchTweetsTask.cancel(true);
        }
    }

    @Override
    public void onDestroy() {
        cancelFetchTweetsTask(true);

        super.onDestroy();
        mapView.onDestroy();
        comm.setAllStreamObjects(allStreamObjects);
    }

    public interface CommunicatorShowMapFragment {
        public void setAllStreamObjects(List<StreamObject> streamObjects);
        public List<ShowMapFragment.StreamObject> getAllStreamObjects();
        public long setStreamStartTime();
        public long setStreamEndTime();
    }

    public List<StreamObject> getAllStreamObjects() {
        return allStreamObjects;
    }
}
