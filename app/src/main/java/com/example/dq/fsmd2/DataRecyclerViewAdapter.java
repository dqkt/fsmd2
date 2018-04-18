package com.example.dq.fsmd2;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataRecyclerViewAdapter extends RecyclerView.Adapter<DataViewHolder> {

    private Context context;
    private List<MonitorData> monitorDataList;
    private LayoutInflater inflater;

    private DecimalFormat vibrationFormat;
    private DecimalFormat positionFormat;

    public DataRecyclerViewAdapter(Context context, List<MonitorData> monitorDataList) {
        this.context = context;
        this.monitorDataList = monitorDataList;
        inflater = LayoutInflater.from(context);

        this.vibrationFormat = new DecimalFormat(".##");
        this.positionFormat = new DecimalFormat(".####");
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final FrameLayout dataLayout = (FrameLayout) inflater.inflate(R.layout.layout_monitor_data, parent, false);
        return new DataViewHolder(dataLayout);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        final MonitorData currentMonitorData = monitorDataList.get(position);
        holder.monitorData = currentMonitorData;

        RelativeLayout statusLayout = (RelativeLayout) holder.regularLayout.findViewById(R.id.layout_status);
        View status = statusLayout.findViewById(R.id.view_status);
        GradientDrawable statusIndicator = (GradientDrawable) status.getBackground();

        VibrationData vibrationData = currentMonitorData.getVibData();
        PositionData positionData = currentMonitorData.getPosData();
        Date timeData = currentMonitorData.getTimeData();

        double vibrationMagnitude = vibrationData.getAvgVibMagnitude();
        String vibrationMagnitudeDisplay = vibrationFormat.format(vibrationMagnitude) + " g";

        int statusCode = Item.determineStatus(vibrationMagnitude);
        statusIndicator.setColor(context.getResources().getColor(Item.getStatusColor(statusCode)));

        String positionDisplay = positionFormat.format(positionData.getLatitude()) + "\u00b0 N, "
                + positionFormat.format(positionData.getLongitude()) + "\u00b0 W";

        holder.vibrationLevel.setText(vibrationMagnitudeDisplay);
        holder.position.setText(positionDisplay);
        holder.time.setText(DateUtils.getRelativeTimeSpanString(timeData.getTime(),
                System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_ALL));
    }

    @Override
    public int getItemCount() {
        return monitorDataList.size();
    }

    public void setMonitorDatas(List<MonitorData> monitorDatas) {
        this.monitorDataList = monitorDatas;
    }
}

class DataViewHolder extends RecyclerView.ViewHolder {

    public MonitorData monitorData;

    public FrameLayout overallLayout;
    public LinearLayout regularLayout;
    public LinearLayout swipeLayout;
    public TextView vibrationLevel;
    public TextView position;
    public TextView time;

    public DataViewHolder(View view) {
        super(view);

        overallLayout = (FrameLayout) view;

        regularLayout = (LinearLayout) view.findViewById(R.id.layout_info);

        vibrationLevel = (TextView) regularLayout.findViewById(R.id.textview_vibration_level);
        position = (TextView) regularLayout.findViewById(R.id.textview_position);
        time = (TextView) regularLayout.findViewById(R.id.textview_time);

        swipeLayout = (LinearLayout) view.findViewById(R.id.layout_swipe_item);

        regularLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
