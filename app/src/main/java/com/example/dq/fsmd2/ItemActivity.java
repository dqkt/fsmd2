package com.example.dq.fsmd2;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DQ on 3/4/2018.
 */

public class ItemActivity extends AppCompatActivity {

    private ItemListViewModel itemListViewModel;
    private MonitorDataListViewModel monitorDataListViewModel;

    private View statusIndicator;
    private TransitionDrawable statusBackground;
    private int colorFrom;
    private int colorTo;
    private TextView dateAdded;
    private TextView ip;

    private RelativeLayout dataAreaLayout;
    private DataRecyclerViewAdapter dataRecyclerViewAdapter;
    private LinearLayoutManager dataLinearLayoutManager;
    private SwipeRefreshLayout dataListRefreshLayout;
    private RecyclerView dataRecyclerView;
    private TextView noDataView;

    private Item item;
    private int itemID;

    private Toolbar itemToolbar;

    private Handler handler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        this.getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        item = (Item) getIntent().getSerializableExtra("ITEM");
        itemID = item.getItemID();

        itemListViewModel = ViewModelProviders.of(this).get(ItemListViewModel.class);

        itemToolbar = findViewById(R.id.action_bar_item);
        setSupportActionBar(itemToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(item.getName());

        Drawable backIcon = itemToolbar.getNavigationIcon();
        backIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        itemToolbar.setNavigationIcon(backIcon);

        setUpSummary();
        setUpDataArea();

        showSummary();

        handler = new Handler();
        populateWithTestData(20);
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

    }
    */

    private void setUpSummary() {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a z 'on' MMM. d, yyyy", Locale.US);

        statusIndicator = (findViewById(R.id.layout_item_status)).findViewById(R.id.view_item_status);
        statusIndicator.setBackgroundColor(getResources().getColor(Item.getStatusColor(item.getStatus())));
        ip = findViewById(R.id.textview_ip);
        ip.setText(item.getIp());

        dateAdded = findViewById(R.id.textview_date_added);
        dateAdded.setText(formatter.format(item.getDateAdded()));
    }

    private void setUpDataArea() {
        dataAreaLayout = findViewById(R.id.view_data_area);
        dataListRefreshLayout = findViewById(R.id.swiperefresh_data);
        dataRecyclerView = findViewById(R.id.recyclerview_item_data);
        noDataView = findViewById(R.id.textview_no_data);

        dataRecyclerViewAdapter = new DataRecyclerViewAdapter(this, new ArrayList<MonitorData>());
        dataRecyclerView.setAdapter(dataRecyclerViewAdapter);
        dataLinearLayoutManager = new LinearLayoutManager(this);
        dataRecyclerView.setLayoutManager(dataLinearLayoutManager);
        ((DefaultItemAnimator) dataRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        dataRecyclerViewAdapter.notifyDataSetChanged();

        monitorDataListViewModel = ViewModelProviders.of(this, new MonitorDataListViewModelFactory(this.getApplication(), itemID)).get(MonitorDataListViewModel.class);
        monitorDataListViewModel.getMonitorDataList().observe(this, new Observer<List<MonitorData>>() {
            @Override
            public void onChanged(@Nullable List<MonitorData> monitorDatas) {
                dataRecyclerViewAdapter.setMonitorDatas(monitorDatas);
                dataRecyclerViewAdapter.notifyDataSetChanged();
                ColorDrawable[] colors = {new ColorDrawable(colorFrom), new ColorDrawable(colorTo)};
                statusBackground = new TransitionDrawable(colors);
                statusIndicator.setBackground(statusBackground);
                statusBackground.startTransition(300);
                dataRecyclerViewAdapter.notifyItemInserted(0);
                if (dataLinearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    dataRecyclerView.scrollToPosition(0);
                }
                dataRecyclerViewAdapter.notifyItemRangeChanged(0, dataRecyclerViewAdapter.getItemCount());
            }
        });

        int safeColor = getResources().getColor(R.color.safeStatusColor);
        int vibratingColor = getResources().getColor(R.color.vibratingStatusColor);
        int lostColor = getResources().getColor(R.color.lostStatusColor);

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

    private void populateWithTestData(final int num) {
        handler.post(new Runnable() {
            int numData = num;

            @Override
            public void run() {
                MonitorData newData;
                Random random = new Random();

                try {
                    if (numData > 0) {
                        newData = new MonitorData(random.nextDouble() * 100, random.nextDouble() * 100, random.nextDouble() * 100,
                                random.nextDouble() * 100, random.nextDouble() * 100, random.nextDouble() * 100,
                                random.nextDouble() * 90, random.nextDouble() * 180, new Date());
                        VibrationData vibrationData = newData.getVibData();
                        colorFrom = getResources().getColor(Item.getStatusColor(item.getStatus()));
                        item.setStatus(Item.determineStatus(VibrationData.calculateVibrationMagnitude(vibrationData.getAvgXVibration(),
                                vibrationData.getAvgYVibration(), vibrationData.getAvgZVibration())));
                        itemListViewModel.updateItem(item);
                        colorTo = getResources().getColor(Item.getStatusColor(item.getStatus()));
                        newData.setItemID(itemID);
                        monitorDataListViewModel.addMonitorData(newData);
                        numData--;
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
}