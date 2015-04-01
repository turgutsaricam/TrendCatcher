package com.turgutsaricam.trendcatcher;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Turgut on 01.04.2015.
 */
public class ShowStreamsFragment extends Fragment {
    DBAdapterStreamSession myTableStreamSession;

    View v;
    ListView listView;

    List<MyListItem> myList = new ArrayList<>();;
    MyListAdapter arrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myTableStreamSession = new DBAdapterStreamSession(getActivity());
        myTableStreamSession.open();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.show_streams_fragment, container, false);
        listView = (ListView) v.findViewById(R.id.lvShowStreamsFragment);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new PopulateList().execute();
    }

    private class PopulateList extends AsyncTask<Void, MyListItem, Void> {

        List<MyListItem> items = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            myList.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            final Cursor cursor = myTableStreamSession.getAll();
            items.clear();
            if(cursor != null && cursor.getCount() > 0) {
                do {
                    MyListItem item = new MyListItem() {
                        {
                            id = cursor.getLong(DBAdapterStreamSession.COL_ROWID);
                            tweetCount = cursor.getInt(DBAdapterStreamSession.COL_TWEET_COUNT);
                            createdAt = cursor.getLong(DBAdapterStreamSession.COL_STARTED_AT);

                            setCreatedAtText();
                        }
                    };

                    items.add(item);
                } while(cursor.moveToNext());
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            myList.addAll(items);
            arrayAdapter = new MyListAdapter(getActivity(), myList);
            Log.e("", "Is listView null: " + (listView == null));
            Log.e("", "Is arrayAdapter null: " + (arrayAdapter == null));

            listView.setAdapter(arrayAdapter);
        }
    }

    private class MyListItem {
        long id;
        int tweetCount = 0;
        long createdAt = 0;

        String createdAtText = "";
        public void setCreatedAtText() {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy @HH:mm");
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(createdAt);

            createdAtText = df.format(c.getTime());
        }
    }

    private class MyListAdapter extends BaseAdapter {
        List<MyListItem> items;
        Context context;

        public MyListAdapter(Context context, List<MyListItem> items) {
            this.items = items;
            this.context = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public MyListItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.show_streams_item, parent, false);

                holder.tvStreamId = (TextView) convertView.findViewById(R.id.tvStreamId);
                holder.tvCreatedAt = (TextView) convertView.findViewById(R.id.tvStreamCreatedAt);
                holder.tvTweetCount = (TextView) convertView.findViewById(R.id.tvStreamTweetCount);
                holder.ivMap = (ImageView) convertView.findViewById(R.id.ivShowBoundaries);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MyListItem item = items.get(position);
            holder.tvStreamId.setText(String.valueOf(item.id));
            holder.tvCreatedAt.setText(item.createdAtText);
            holder.tvTweetCount.setText("Tweet count: " + item.tweetCount);
            holder.ivMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Show boundaries", Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }
    }

    private class ViewHolder {
        TextView tvTweetCount;
        TextView tvStreamId;
        TextView tvCreatedAt;
        ImageView ivMap;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(myTableStreamSession != null) myTableStreamSession.close();
    }
}
