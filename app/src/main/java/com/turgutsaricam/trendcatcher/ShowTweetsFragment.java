package com.turgutsaricam.trendcatcher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

/**
 * Created by Turgut on 08.03.2015.
 */
public class ShowTweetsFragment extends Fragment {
    View v;
    ListView lvShowTweets;

    CommunicatorShowTweetsFragment comm;
    List<Status> allTweets = new ArrayList<Status>();
    List<Status> loadedTweets = new ArrayList<Status>();

    GenericAdapter<Status> arrayAdapter;

    private final int TWEET_PER_PAGE = 50;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        comm = (CommunicatorShowTweetsFragment) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.show_tweets_fragment, container, false);
        lvShowTweets = (ListView) v.findViewById(R.id.lvShowTweets);

        lvShowTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.e("", "Load more tweets");
                loadMoreTweets(page, totalItemsCount);
            }
        });

        lvShowTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Status currentTweet = loadedTweets.get(position);
                String url = MyConstants.TWITTER_URL + currentTweet.getUser().getScreenName()
                        + MyConstants.TWITTER_STATUS_BASE + currentTweet.getId();

                if(url == null) {
                    Log.e("", "URL is null");
                    return;
                }

                if (!url.startsWith(MyConstants.HTTP) && !url.startsWith(MyConstants.HTTPS)) {
                    url = MyConstants.HTTP + url;
                }

                Log.e("", "URL: " + url);

                Uri uri = Uri.parse(url);

                intent.setData(uri);
                startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadTweets();
    }

    private void loadTweets() {
        List<ShowMapFragment.StreamObject> allStreamObjects = comm.requestStreamObjects();
        Log.e("", "allStreamObjects size is " + allStreamObjects.size());
        for(ShowMapFragment.StreamObject so : allStreamObjects) {
            allTweets.addAll(so.getTweets());
        }

        // Reverse the list in order to load tweets so that earliest posted one is at the top
        Collections.reverse(allTweets);

        final int count = TWEET_PER_PAGE >= allTweets.size() ? allTweets.size() : TWEET_PER_PAGE;
        final List<Status> subList = allTweets.subList(0, count);
        loadedTweets.addAll(subList);

        arrayAdapter = new GenericAdapter<Status>(getActivity(), loadedTweets) {
            @Override
            public View getDataRow(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;

                if(convertView == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.show_tweet_item, parent, false);

                    holder = new ViewHolder();
                    holder.ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfileImage);
                    holder.tvText = (TextView) convertView.findViewById(R.id.tvTweetText);
                    holder.tvName = (TextView) convertView.findViewById(R.id.tvTweetName);
                    holder.tvScreenName = (TextView) convertView.findViewById(R.id.tvTweetScreenName);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                Status status = loadedTweets.get(position);

                holder.tvText.setText(getColoredTweet(status.getText()));

                holder.tvName.setText(status.getUser().getName());
                holder.tvScreenName.setText("@" + status.getUser().getScreenName());

                String url = loadedTweets.get(position).getUser().getProfileImageURL();
                Picasso.with(getActivity())
                        .load(url)
                        .fit()
                        .centerCrop()
                        .into(holder.ivProfileImage);


                return convertView;
            }
        };

        arrayAdapter.setServerListSize(allTweets.size());
        lvShowTweets.setAdapter(arrayAdapter);
    }

    private void loadMoreTweets(int page, int totalItemsCount) {
        int startIndex = totalItemsCount - 1;
        int endIndex = TWEET_PER_PAGE * page;
        endIndex = endIndex >= allTweets.size() ? allTweets.size() : endIndex;

        if(startIndex != endIndex) {
            List<Status> subList = allTweets.subList(startIndex, endIndex);
            loadedTweets.addAll(subList);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    private SpannableStringBuilder getColoredTweet(String tweetText) {
        Pattern mentionPattern = Pattern.compile("(?<=(?:^|\\W)@)([A-Za-z_\\d\u00E7\u011F\u0131\u015F\u00F6\u00FC\u00C7\u011E\u0130\u015E\u00D6\u00DC]+)");
        Pattern hashtagPattern = Pattern.compile("(?<=(?:^|\\W)#)([A-Za-z_\\d\u00E7\u011F\u0131\u015F\u00F6\u00FC\u00C7\u011E\u0130\u015E\u00D6\u00DC]+)");
        Pattern urlPattern = Patterns.WEB_URL;

        SpannableStringBuilder spannable = new SpannableStringBuilder(tweetText);

        // HashTags
        Matcher matcher = hashtagPattern.matcher(tweetText);
        while (matcher.find()) {
            final ForegroundColorSpan colorHashTag = new ForegroundColorSpan(Color.rgb(51, 105, 30));
            spannable.setSpan(colorHashTag, matcher.start() - 1, matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Mentions
        matcher = mentionPattern.matcher(tweetText);
        while (matcher.find()) {
            final StyleSpan mentionStyle = new StyleSpan(Typeface.BOLD);
            spannable.setSpan(mentionStyle, matcher.start() - 1, matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // URLs
        matcher = urlPattern.matcher(tweetText);
        while (matcher.find()) {
            final ForegroundColorSpan colorURL = new ForegroundColorSpan(Color.rgb(85, 172, 238));
            spannable.setSpan(colorURL, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }

    private class ViewHolder {
        ImageView ivProfileImage;
        TextView tvText;
        TextView tvScreenName;
        TextView tvName;
    }

    /**
     *  A child class shall subclass this Adapter and
     *  implement method getDataRow(int position, View convertView, ViewGroup parent),
     *  which supplies a View present data in a ListRow.
     *
     *  This parent Adapter takes care of displaying ProgressBar in a row or
     *  indicating that it has reached the last row.
     *
     */
    public abstract class GenericAdapter<T> extends BaseAdapter {

        // the main data list to save loaded data
        protected List<T> dataList;

        protected Activity mActivity;

        // the serverListSize is the total number of items on the server side,
        // which should be returned from the web request results
        protected int serverListSize = -1;

        // Two view types which will be used to determine whether a row should be displaying
        // data or a Progressbar
        public static final int VIEW_TYPE_LOADING = 0;
        public static final int VIEW_TYPE_ACTIVITY = 1;


        public GenericAdapter(Activity activity, List<T> list) {
            mActivity = activity;
            dataList = list;
        }

        public void setServerListSize(int serverListSize){
            this.serverListSize = serverListSize;
        }

        /**
         * disable click events on indicating rows
         */
        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) == VIEW_TYPE_ACTIVITY;
        }

        /**
         * One type is normal data row, the other type is Progressbar
         */
        @Override
        public int getViewTypeCount() {
            return 2;
        }

        /**
         * the size of the List plus one, the one is the last row, which displays a Progressbar
         */
        @Override
        public int getCount() {
            return dataList.size() + 1;
        }

        /**
         * return the type of the row,
         * the last row indicates the user that the ListView is loading more data
         */
        @Override
        public int getItemViewType(int position) {
            return (position >= dataList.size()) ? VIEW_TYPE_LOADING : VIEW_TYPE_ACTIVITY;
        }

        @Override
        public T getItem(int position) {
            return (getItemViewType(position) == VIEW_TYPE_ACTIVITY) ? dataList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return (getItemViewType(position) == VIEW_TYPE_ACTIVITY) ? position : -1;
        }

        /**
         *  returns the correct view
         */
        @Override
        public  View getView(int position, View convertView, ViewGroup parent){
            if (getItemViewType(position) == VIEW_TYPE_LOADING) {
                    // display the last row
                    return getFooterView(position, convertView, parent);
            }
            View dataRow = convertView;
            dataRow = getDataRow(position, convertView, parent);

            return dataRow;
        };

        /**
         * A subclass should override this method to supply the data row.
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        public abstract View getDataRow(int position, View convertView, ViewGroup parent);

        /**
         * returns a View to be displayed in the last row.
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        public View getFooterView(int position, View convertView,
                                  ViewGroup parent) {
            if (position >= serverListSize && serverListSize > 0) {
                // the ListView has reached the last row
                TextView tvLastRow = new TextView(mActivity);
                tvLastRow.setHint("Reached the last row.");
                tvLastRow.setGravity(Gravity.CENTER);
                return tvLastRow;
            }

            View row = convertView;
            if (row == null) {
                row = mActivity.getLayoutInflater().inflate(
                        R.layout.progress, parent, false);
            }

            return row;
        }

    }

    public interface CommunicatorShowTweetsFragment {
        public List<ShowMapFragment.StreamObject> requestStreamObjects();
    }
}
