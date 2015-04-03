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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Turgut on 01.04.2015.
 */
public class ShowStreamsFragment extends Fragment {
    DBAdapterStreamSession myTableStreamSession;

    View v;
    ListView listView;
    TextView tvInfo;

    List<MyListItem> myList = new ArrayList<>();;
    MyListAdapter arrayAdapter;

//    List<MyListItem> listMonday = new ArrayList<>();
//    List<MyListItem> listTuesday = new ArrayList<>();
//    List<MyListItem> listWednesday = new ArrayList<>();
//    List<MyListItem> listThursday = new ArrayList<>();
//    List<MyListItem> listFriday = new ArrayList<>();
//    List<MyListItem> listSaturday = new ArrayList<>();
//    List<MyListItem> listSunday = new ArrayList<>();

    // This should be set as Calendar."DAY"     e.g. Calendar.MONDAY, Calendar.TUESDAY...
    int loadedDay = -1;

    long totalTweetCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myTableStreamSession = new DBAdapterStreamSession(getActivity());
        myTableStreamSession.open();

        if(getArguments() != null) {
            loadedDay = getArguments().getInt("loaded_day", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.show_streams_fragment, container, false);
        listView = (ListView) v.findViewById(R.id.lvShowStreamsFragment);
        tvInfo = (TextView) v.findViewById(R.id.tvStreamInfo);
        tvInfo.setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new PopulateList().execute();
    }

    private void clearLists() {
        myList.clear();
//        listMonday.clear();
//        listTuesday.clear();
//        listWednesday.clear();
//        listThursday.clear();
//        listFriday.clear();
//        listSaturday.clear();
//        listSunday.clear();
    }

    private class PopulateList extends AsyncTask<Void, MyListItem, Void> {

        List<MyListItem> items = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            clearLists();
        }

        @Override
        protected Void doInBackground(Void... params) {
            final Cursor cursor = myTableStreamSession.getAll();

            final Calendar calendar = Calendar.getInstance();

            if(cursor != null && cursor.getCount() > 0) {
                do {
                    final long startedAt = cursor.getLong(DBAdapterStreamSession.COL_STARTED_AT);
                    calendar.setTimeInMillis(startedAt);

                    if(calendar.get(Calendar.DAY_OF_WEEK) == loadedDay) {
                        MyListItem item = new MyListItem() {
                            {
                                id = cursor.getLong(DBAdapterStreamSession.COL_ROWID);
                                tweetCount = cursor.getInt(DBAdapterStreamSession.COL_TWEET_COUNT);
                                createdAt = startedAt;

                                totalTweetCount += tweetCount;
                                setCreatedAtText();
                            }
                        };

                        items.add(item);

//                    switch (calendar.get(Calendar.DAY_OF_WEEK)) {
//                        case Calendar.MONDAY:
//                            listMonday.add(item);
//                            break;
//                        case Calendar.TUESDAY:
//                            listTuesday.add(item);
//                            break;
//                        case Calendar.WEDNESDAY:
//                            listWednesday.add(item);
//                            break;
//                        case Calendar.THURSDAY:
//                            listThursday.add(item);
//                            break;
//                        case Calendar.FRIDAY:
//                            listFriday.add(item);
//                            break;
//                        case Calendar.SATURDAY:
//                            listSaturday.add(item);
//                            break;
//                        case Calendar.SUNDAY:
//                            listSunday.add(item);
//                            break;
//                    }
                    }
                } while(cursor.moveToNext());
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            sortByTimeAsc(items);
            myList.addAll(items);
            arrayAdapter = new MyListAdapter(getActivity(), myList);
            Log.e("", "Is listView null: " + (listView == null));
            Log.e("", "Is arrayAdapter null: " + (arrayAdapter == null));

            listView.setAdapter(arrayAdapter);
            tvInfo.setText(totalTweetCount + " tweets");
            tvInfo.setVisibility(View.VISIBLE);
        }
    }

    private void sortByTimeAsc(List<MyListItem> list) {
        Collections.sort(list, new Comparator<MyListItem>() {
            @Override
            public int compare(MyListItem lhs, MyListItem rhs) {
                int h1 = lhs.hour;
                int h2 = rhs.hour;

                if(h1 > h2) {
                    return 1;
                } else if(h1 < h2) {
                    return -1;
                }

                return 0;
            }
        });
    }

    private class MyListItem {
        long id;
        int tweetCount = 0;
        long createdAt = 0;

        int hour = -1;

        String createdAtText = "";
        public void setCreatedAtText() {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy @HH:mm");
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(createdAt);

            createdAtText = df.format(c.getTime());
            hour = c.get(Calendar.HOUR_OF_DAY);
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
                holder.tvHour = (TextView) convertView.findViewById(R.id.tvStreamHour);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MyListItem item = items.get(position);
            holder.tvStreamId.setText(String.valueOf(item.id));
            holder.tvCreatedAt.setText(item.createdAtText);
            holder.tvTweetCount.setText("Tweet count: " + item.tweetCount);
            holder.tvHour.setText(String.valueOf(item.hour));

            return convertView;
        }
    }

    private class ViewHolder {
        TextView tvTweetCount;
        TextView tvStreamId;
        TextView tvCreatedAt;
        TextView tvHour;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(myTableStreamSession != null) myTableStreamSession.close();
    }
}
