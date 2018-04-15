package com.example.dq.fsmd2;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DataRecyclerViewAdapter extends RecyclerView.Adapter<DataViewHolder> {

    private Context context;
    private ArrayList<MonitorData> monitorDataList;
    private LayoutInflater inflater;

    private DecimalFormat vibrationFormat;
    private DecimalFormat positionFormat;

    public DataRecyclerViewAdapter(Context context, ArrayList<MonitorData> monitorDataList) {
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

        /*
        final int currentPosition = position;
        holder.regularLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ItemActivity.class);
                i.putExtra("Selected Data", monitorDataList.get(currentPosition));
                context.startActivity(i);
            }
        });
        */

        RelativeLayout statusLayout = (RelativeLayout) holder.regularLayout.findViewById(R.id.layout_status);
        View status = statusLayout.findViewById(R.id.view_status);
        GradientDrawable statusIndicator = (GradientDrawable) status.getBackground();

        VibrationData vibrationData = currentMonitorData.getVibrationData();
        PositionData positionData = currentMonitorData.getPositionData();
        TimeData timeData = currentMonitorData.getTimeData();

        double vibrationMagnitude = vibrationData.getAvgVibMagnitude();
        String vibrationMagnitudeDisplay = vibrationFormat.format(vibrationMagnitude) + " g";

        int statusCode = Item.determineStatus(vibrationMagnitude);
        statusIndicator.setColor(context.getResources().getColor(Item.getStatusColor(statusCode)));

        String positionDisplay = positionFormat.format(positionData.getLatitude()) + "\u00b0 N, "
                + positionFormat.format(positionData.getLongitude()) + "\u00b0 W";

        holder.vibrationLevel.setText(vibrationMagnitudeDisplay);
        holder.position.setText(positionDisplay);
        holder.time.setText(DateUtils.getRelativeTimeSpanString(timeData.getDate().getTime(),
                System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_ALL));
    }

    @Override
    public int getItemCount() {
        return monitorDataList.size();
    }
}

class DataViewHolder extends RecyclerView.ViewHolder {

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
    }
}
