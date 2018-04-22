package com.example.dq.fsmd2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.transition.ChangeBounds;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataRecyclerViewAdapter extends RecyclerView.Adapter<DataViewHolder> {

    private Context context;
    private List<MonitorData> monitorDataList;
    private SimpleDateFormat formatter;
    private LayoutInflater inflater;

    private DecimalFormat vibrationFormat;
    private DecimalFormat positionFormat;

    public DataRecyclerViewAdapter(Context context, List<MonitorData> monitorDataList) {
        this.context = context;
        this.monitorDataList = monitorDataList;
        this.formatter = new SimpleDateFormat("h:mm a z 'on' MMM. d, yyyy", Locale.US);
        this.inflater = LayoutInflater.from(context);

        this.vibrationFormat = new DecimalFormat("0.##");
        this.positionFormat = new DecimalFormat("0.####");
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LinearLayout dataLayout = (LinearLayout) inflater.inflate(R.layout.layout_monitor_data, parent, false);
        return new DataViewHolder(dataLayout);
    }

    @Override
    public void onBindViewHolder(final DataViewHolder holder, int position) {
        final MonitorData currentMonitorData = monitorDataList.get(position);
        holder.monitorData = currentMonitorData;

        RelativeLayout statusLayout = holder.regularLayout.findViewById(R.id.layout_status);
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

        holder.xVibrationLevel.setText(String.valueOf("X: " + vibrationFormat.format(vibrationData.getAvgXVibration()) + " g"));
        holder.yVibrationLevel.setText(String.valueOf("Y: " + vibrationFormat.format(vibrationData.getAvgYVibration()) + " g"));
        holder.zVibrationLevel.setText(String.valueOf("Z: " + vibrationFormat.format(vibrationData.getAvgZVibration()) + " g"));

        holder.timeReceived.setText(formatter.format(timeData));
    }

    @Override
    public int getItemCount() {
        return monitorDataList.size();
    }

    public void setMonitorDatas(List<MonitorData> monitorDatas) {
        this.monitorDataList = monitorDatas;
    }
}

class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private int originalHeight = 0;
    private boolean isViewExpanded = false;

    public MonitorData monitorData;

    public LinearLayout overallLayout;
    public LinearLayout regularLayout;
    public LinearLayout detailedLayout;

    public LinearLayout swipeLayout;
    public TextView vibrationLevel;
    public TextView position;
    public TextView time;

    public TextView xVibrationLevel;
    public TextView yVibrationLevel;
    public TextView zVibrationLevel;
    public TextView timeReceived;

    public DataViewHolder(View view) {
        super(view);

        overallLayout = (LinearLayout) view;

        regularLayout = view.findViewById(R.id.layout_info);
        detailedLayout = view.findViewById(R.id.layout_details);

        vibrationLevel = regularLayout.findViewById(R.id.textview_vibration_level);
        position = regularLayout.findViewById(R.id.textview_position);
        time = regularLayout.findViewById(R.id.textview_time);

        xVibrationLevel = regularLayout.findViewById(R.id.textview_x_vib);
        yVibrationLevel = regularLayout.findViewById(R.id.textview_y_vib);
        zVibrationLevel = regularLayout.findViewById(R.id.textview_z_vib);
        timeReceived = regularLayout.findViewById(R.id.textview_time_received);

        regularLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        if (originalHeight == 0) {
            originalHeight = view.getHeight();
        }

        ValueAnimator valueAnimator;
        if (!isViewExpanded) {
            detailedLayout.setVisibility(View.VISIBLE);
            detailedLayout.setEnabled(true);
            isViewExpanded = true;
            valueAnimator = ValueAnimator.ofInt(originalHeight, originalHeight + (int) (originalHeight * 2.0));
        } else {
            isViewExpanded = false;
            valueAnimator = ValueAnimator.ofInt(originalHeight + (int) (originalHeight * 2.0), originalHeight);

            Animation a = new AlphaAnimation(1.00f, 0.00f);

            a.setDuration(200);
            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    detailedLayout.setVisibility(View.GONE);
                    detailedLayout.setEnabled(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            detailedLayout.startAnimation(a);
        }
        valueAnimator.setDuration(200);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                view.getLayoutParams().height = value.intValue();
                view.requestLayout();
            }
        });
        valueAnimator.start();
    }
}
