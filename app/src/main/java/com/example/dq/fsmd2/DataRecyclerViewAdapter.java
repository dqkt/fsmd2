package com.example.dq.fsmd2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataRecyclerViewAdapter extends RecyclerView.Adapter<DataViewHolder> {

    private Context context;
    private List<MonitorData> monitorDataList;
    private LayoutInflater inflater;

    private DecimalFormat vibrationFormat;
    private DecimalFormat detailedVibrationFormat;
    private DecimalFormat positionFormat;

    public DataRecyclerViewAdapter(Context context, List<MonitorData> monitorDataList) {
        this.context = context;
        this.monitorDataList = monitorDataList;
        this.inflater = LayoutInflater.from(context);

        this.vibrationFormat = new DecimalFormat("0.00");
        this.detailedVibrationFormat = new DecimalFormat("0.0000");
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

        if (currentMonitorData.isViewExpanded()) {
            holder.time.setText(holder.formatter.format(timeData));
        } else {
            holder.time.setText(DateUtils.getRelativeTimeSpanString(timeData.getTime(),
                    System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_ALL));
        }

        holder.avgXVibrationLevel.setText(detailedVibrationFormat.format(vibrationData.getAvgXVibration()));
        holder.avgYVibrationLevel.setText(detailedVibrationFormat.format(vibrationData.getAvgYVibration()));
        holder.avgZVibrationLevel.setText(detailedVibrationFormat.format(vibrationData.getAvgZVibration()));

        holder.peakXVibrationLevel.setText(detailedVibrationFormat.format(vibrationData.getPeakXVibration()));
        holder.peakYVibrationLevel.setText(detailedVibrationFormat.format(vibrationData.getPeakYVibration()));
        holder.peakZVibrationLevel.setText(detailedVibrationFormat.format(vibrationData.getPeakZVibration()));
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

    private int originalHeight;

    public MonitorData monitorData;

    public LinearLayout overallLayout;
    public LinearLayout regularLayout;
    public RelativeLayout detailedLayout;

    public TextView vibrationLevel;
    public TextView position;
    public TextView time;

    public SimpleDateFormat formatter;

    public TextView avgXVibrationLevel;
    public TextView avgYVibrationLevel;
    public TextView avgZVibrationLevel;
    public TextView peakXVibrationLevel;
    public TextView peakYVibrationLevel;
    public TextView peakZVibrationLevel;

    public DataViewHolder(View view) {
        super(view);

        originalHeight = 0;

        overallLayout = (LinearLayout) view;

        regularLayout = view.findViewById(R.id.layout_info);
        detailedLayout = view.findViewById(R.id.layout_details);

        vibrationLevel = regularLayout.findViewById(R.id.textview_vibration_level);
        position = regularLayout.findViewById(R.id.textview_position);
        time = regularLayout.findViewById(R.id.textview_time);

        formatter = new SimpleDateFormat("h:mm:ss a z'\n'M/d/yyyy", Locale.US);

        avgXVibrationLevel = regularLayout.findViewById(R.id.textview_avg_x_vib);
        avgYVibrationLevel = regularLayout.findViewById(R.id.textview_avg_y_vib);
        avgZVibrationLevel = regularLayout.findViewById(R.id.textview_avg_z_vib);

        peakXVibrationLevel = regularLayout.findViewById(R.id.textview_peak_x_vib);
        peakYVibrationLevel = regularLayout.findViewById(R.id.textview_peak_y_vib);
        peakZVibrationLevel = regularLayout.findViewById(R.id.textview_peak_z_vib);

        regularLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        if (originalHeight == 0) {
            originalHeight = view.getHeight();
        }

        ValueAnimator valueAnimator;
        if (!monitorData.isViewExpanded()) {
            time.setText(formatter.format(monitorData.getTimeData()));
            detailedLayout.setVisibility(View.VISIBLE);
            detailedLayout.setEnabled(true);
            monitorData.setViewExpanded(true);
            valueAnimator = ValueAnimator.ofInt(originalHeight, originalHeight + (int) (originalHeight * 1.8));
        } else {
            time.setText(DateUtils.getRelativeTimeSpanString(monitorData.getTimeData().getTime(),
                    System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_ALL));
            monitorData.setViewExpanded(false);
            valueAnimator = ValueAnimator.ofInt(originalHeight + (int) (originalHeight * 1.8), originalHeight);

            Animation detailsFade = new AlphaAnimation(1.00f, 0.00f);

            detailsFade.setDuration(200);
            detailsFade.setAnimationListener(new Animation.AnimationListener() {
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

            detailedLayout.startAnimation(detailsFade);
        }
        valueAnimator.setDuration(200);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                view.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                view.requestLayout();
            }
        });
        valueAnimator.start();
    }

}
