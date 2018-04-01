package com.example.dq.fsmd2;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DQ on 3/4/2018.
 */

public class ItemActivity extends AppCompatActivity {

    private TextView dateAdded;
    private TextView ip;

    private RelativeLayout dataAreaLayout;
    private ArrayList<MonitorData> dataList;
    private DataRecyclerViewAdapter dataRecyclerViewAdapter;
    private SwipeRefreshLayout dataListRefreshLayout;
    private RecyclerView dataRecyclerView;
    private TextView noDataView;

    private Item item;

    private Toolbar itemToolbar;

    private Timer timer;
    private Handler handler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        this.getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        item = (Item) getIntent().getSerializableExtra("Selected Item");

        itemToolbar = (Toolbar) findViewById(R.id.action_bar_item);
        setSupportActionBar(itemToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        timer = new Timer();

        setTitle(item.getName());

        Drawable backIcon = itemToolbar.getNavigationIcon();
        backIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        itemToolbar.setNavigationIcon(backIcon);

        setUpSummary();
        setUpDataArea();

        showSummary();

        handler = new Handler();
        handler.post(new Runnable() {
            int numData = 100;

            @Override
            public void run() {
                MonitorData newData;
                Random random = new Random();

                try {
                    if (numData > 0) {
                        newData = new MonitorData(random.nextDouble() * 100, random.nextDouble() * 100, random.nextDouble() * 100,
                                random.nextDouble() * 100, random.nextDouble() * 100, random.nextDouble() * 100,
                                random.nextDouble() * 90, random.nextDouble() * 180, new Date());
                        dataList.add(0, newData);
                        numData--;
                        dataRecyclerViewAdapter.notifyItemInserted(0);
                        dataRecyclerViewAdapter.notifyItemRangeChanged(0, dataList.size());
                        dataRecyclerView.scrollToPosition(0);
                        showDataVisible();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

    }
    */

    private void setUpSummary() {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a z'\n'MMM. d, yyyy");

        ip = (TextView) findViewById(R.id.textview_ip);
        ip.setText(item.getIP());

        dateAdded = (TextView) findViewById(R.id.textview_date_added);
        dateAdded.setText(formatter.format(item.getDateAdded()));
    }

    private void setUpDataArea() {
        dataList = new ArrayList<MonitorData>();

        dataAreaLayout = (RelativeLayout) findViewById(R.id.view_data_area);
        dataListRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_data);
        dataRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_item_data);
        noDataView = (TextView) findViewById(R.id.textview_no_data);

        dataRecyclerViewAdapter = new DataRecyclerViewAdapter(this, dataList);
        dataRecyclerView.setAdapter(dataRecyclerViewAdapter);
        dataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataRecyclerViewAdapter.notifyDataSetChanged();

        int safeColor = getResources().getColor(R.color.safeStatusColor);
        int vibratingColor = getResources().getColor(R.color.vibratingStatusColor);
        int lostColor = getResources().getColor(R.color.lostStatusColor);

        GradientDrawable dataBackground = (GradientDrawable) dataRecyclerView.getBackground();
        dataBackground.setColor(Color.WHITE);

        dataRecyclerView.setBackground(dataBackground);

        dataListRefreshLayout.setColorSchemeColors(safeColor, vibratingColor, lostColor);
        dataListRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dataRecyclerViewAdapter.notifyDataSetChanged();
                dataListRefreshLayout.setRefreshing(false);
            }
        });

        dataRecyclerView.bringToFront();
    }

    private void showSummary() {

    }

    private void showNoData() {
        noDataView.setVisibility(View.VISIBLE);
        dataAreaLayout.invalidate();
    }

    private void showDataVisible() {
        noDataView.setVisibility(View.INVISIBLE);
        dataRecyclerView.bringToFront();
        dataAreaLayout.invalidate();
    }

    private void populateWithTestData(int numData, int intervalSeconds) {
        timer.schedule(new AddTestDataTask(100), 0, 1000);
        dataRecyclerViewAdapter.notifyDataSetChanged();
        showDataVisible();
    }


    class AddTestDataTask extends TimerTask {
        int numData;

        public AddTestDataTask(int numData) {
            this.numData = numData;
        }

        @Override
        public void run() {
            MonitorData newData;
            Random random = new Random();

            if (numData > 0) {
                newData = new MonitorData(random.nextDouble() * 100, random.nextDouble() * 100, random.nextDouble() * 100,
                        random.nextDouble() * 100, random.nextDouble() * 100, random.nextDouble() * 100,
                        random.nextDouble() * 90, random.nextDouble() * 180, new Date());
                dataList.add(0, newData);
                numData--;
            }
        }
    }
}