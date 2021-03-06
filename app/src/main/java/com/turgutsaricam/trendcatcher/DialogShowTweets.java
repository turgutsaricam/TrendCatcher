package com.turgutsaricam.trendcatcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.LoadCallback;
import com.twitter.sdk.android.tweetui.TweetViewFetchAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import twitter4j.Status;

/**
 * Created by Turgut on 04.03.2015.
 */
public class DialogShowTweets extends DialogFragment {

    View v;
    ListView mListView;
    ProgressBar pBar;

    CommunicatorDialogShowTweets comm;
    List<ShowMapFragment.StreamObject> mAllStreamObjects = new ArrayList<ShowMapFragment.StreamObject>();
    String timeDifference = "";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        comm = (CommunicatorDialogShowTweets) activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.show_tweets_fragment, null);
        mListView = (ListView) v.findViewById(R.id.lvShowTweets);
        pBar = (ProgressBar) v.findViewById(R.id.stfProgressBar);
        pBar.setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        Button refresh = dialog.getButton(Dialog.BUTTON_POSITIVE);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
                populateList();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        populateList();
    }

    private void populateList() {
        pBar.setVisibility(View.VISIBLE);
        mAllStreamObjects.clear();
        mAllStreamObjects.addAll(comm.requestStreamObjects());
//        Collections.reverse(mAllStreamObjects);

        timeDifference = comm.requestTimeDifference();

        // Get tweet ids
        List<Long> tweetIds = new ArrayList<Long>();
        for(ShowMapFragment.StreamObject so : mAllStreamObjects) {
            for (ShowMapFragment.StatusObject status : so.getStatusObjects()) {
                tweetIds.add(status.getTweetId());
            }
        }

        Collections.reverse(tweetIds);

        // Set up the adapter and add tweets
        final TweetViewFetchAdapter adapter = new TweetViewFetchAdapter<CompactTweetView>(getActivity());
        final AlertDialog dialog = (AlertDialog) getDialog();
        mListView.setAdapter(adapter);

        List<Long> subList;
        final int count = 50 > tweetIds.size() ? tweetIds.size() : 50;
        subList = tweetIds.subList(0, count);
        adapter.setTweetIds(subList, new LoadCallback<List<Tweet>>() {
            @Override
            public void success(List<Tweet> tweets) {
                pBar.setVisibility(View.GONE);

                Button neutralButton = dialog.getButton(Dialog.BUTTON_NEUTRAL);
                String s = count > 1 ? "s" : "";
                int totalTweetCount = 0;
                for(ShowMapFragment.StreamObject so : mAllStreamObjects) {
                    totalTweetCount += so.getStatusObjects().size();
                }
                neutralButton.setText("OK (Last " + count + "/" + totalTweetCount + " tweet" + s + " - " + timeDifference + ")");
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(getActivity(), "Failed to load tweets. " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface CommunicatorDialogShowTweets {
        public List<ShowMapFragment.StreamObject> requestStreamObjects();
        public String requestTimeDifference();
    }
}
