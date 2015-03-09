package com.turgutsaricam.trendcatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;

/**
 * Created by Turgut on 07.03.2015.
 */
public class ProfilePhotosFragment extends Fragment {

    View v;
    GridView gvProfilePhotos;
    MyListAdapter arrayAdapter;

    CommunicatorProfilePhotosFragment comm;
    List<ShowMapFragment.StatusObject> allTweets;

    private boolean loadTweetPhotos = false;
    public static String LOAD_TWEET_PHOTOS = "loadTweetPhotos";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        comm = (CommunicatorProfilePhotosFragment) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.profile_photos_fragment, container, false);

        gvProfilePhotos = (GridView) v.findViewById(R.id.gvProfilePhotos);
        gvProfilePhotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Status currentTweet = allTweets.get(position).getStatus();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                String url = "";
                if(!loadTweetPhotos) {
                    url = MyConstants.TWITTER_URL + currentTweet.getUser().getScreenName();
                } else {
                    url = MyConstants.TWITTER_URL + currentTweet.getUser().getScreenName()
                            + MyConstants.TWITTER_STATUS_BASE + currentTweet.getId();
                }

                if(url != null) {
                    if (!url.startsWith(MyConstants.HTTP) && !url.startsWith(MyConstants.HTTPS)) {
                        url = MyConstants.HTTP + url;
                    }

                    logIt("URL: " + url);

                    Uri uri = Uri.parse(url);

                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "No Profile URL", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getArguments() != null) {
            loadTweetPhotos = getArguments().getBoolean(LOAD_TWEET_PHOTOS, false);
        }
        populateGrid();
    }

    private void logIt(String text) {
        Log.e("", text);
    }

    private void populateGrid() {
        List<ShowMapFragment.StreamObject> streamObjects = comm.getStreamObjects();
//        logIt("Stream count: " + streamObjects.size());

        allTweets = new ArrayList<ShowMapFragment.StatusObject>();

        if(!loadTweetPhotos) {
            for (ShowMapFragment.StreamObject so : streamObjects) {
                allTweets.addAll(so.getStatusObjects());
            }
        } else {
            for (ShowMapFragment.StreamObject so : streamObjects) {
                for (ShowMapFragment.StatusObject statusObject : so.getStatusObjects()) {
//                    MediaEntity[] mediaEntities = tweet.getMediaEntities();
//                    if(mediaEntities != null) {
//                        for (MediaEntity me : mediaEntities) {
//                            if (me.getType().matches("photo")) {
//                                allTweets.add(tweet);
//                            }
//                        }
//                    }
                    Status tweet = statusObject.getStatus();
                    MediaEntity[] mediaEntities = tweet.getExtendedMediaEntities();
                    if(mediaEntities != null) {
                        for (MediaEntity me : mediaEntities) {
                            if (me.getType().matches("photo")) {
                                allTweets.add(statusObject);
                            }
                        }
                    }
                }
            }
        }

//        logIt("Tweet count: " + allTweets.size());
        makeToast(allTweets.size() + " photos will be loaded");
        arrayAdapter = new MyListAdapter(getActivity(), allTweets);
        gvProfilePhotos.setAdapter(arrayAdapter);
    }

    private void makeToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    private class MyListAdapter extends BaseAdapter {

        Context context;
        List<ShowMapFragment.StatusObject> tweets;

        public MyListAdapter(Context context, List<ShowMapFragment.StatusObject> tweets) {
            this.context = context;
            this.tweets = tweets;
        }

        @Override
        public int getCount() {
            return tweets.size();
        }

        @Override
        public Object getItem(int position) {
            return tweets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return tweets.get(position).getStatus().getId();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            SquaredImageView view = (SquaredImageView) convertView;
            if(view == null) {
                view = new SquaredImageView(context);
            }

            String url = "";
            if(!loadTweetPhotos) {
                url = tweets.get(position).getStatus().getUser().getProfileImageURL();
            } else {
                Status currentStatus = tweets.get(position).getStatus();
                url = currentStatus.getExtendedMediaEntities()[findOccurrencesBefore(currentStatus, position)].getMediaURL();
            }

            Picasso.with(context)
                    .load(url)
                    .fit()
                    .centerCrop()
                    .into(view);

            return view;
        }

        private int findOccurrencesBefore(Status tweet, int position) {
            if(position == 0) return 0;

            int occurrences = 0;
            for(int i = 0; i < position; i++) {
                if(tweets.get(i).equals(tweet)) {
                    occurrences++;
                }
            }

            Log.e("", "Occurrences: " + occurrences);
            return occurrences;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile_photos_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pffInfo:
                new AlertDialog.Builder(getActivity())
                        .setMessage(allTweets.size() + " photos are found.")
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface CommunicatorProfilePhotosFragment {
        public List<ShowMapFragment.StreamObject> getStreamObjects();
    }

}
