package com.example.dq.fsmd2;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private Context context;
    private ArrayList<Item> items;
    private ArrayList<Item> itemsPendingRemoval;
    private LayoutInflater inflater;

    private static final int PENDING_REMOVAL_TIMEOUT = 5000;
    private Handler handler = new Handler();
    HashMap<String, Runnable> pendingRunnables = new HashMap<>();
    private ItemTouchHelper itemTouchHelper;

    public ItemRecyclerViewAdapter(Context context, ArrayList<Item> items) {
        this.context = context;
        this.items = items;
        itemsPendingRemoval = new ArrayList<Item>();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final FrameLayout itemLayout = (FrameLayout) inflater.inflate(R.layout.layout_item, parent, false);
        return new ItemViewHolder(itemLayout);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {

        final Item currentItem = items.get(position);

        final int currentPosition = position;
        holder.regularLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ItemActivity.class);
                i.putExtra("Selected Item", currentItem);
                context.startActivity(i);
            }
        });

        int statusCode = currentItem.getStatus();

        RelativeLayout statusLayout = (RelativeLayout) holder.regularLayout.findViewById(R.id.layout_status);
        View status = statusLayout.findViewById(R.id.view_status);
        GradientDrawable statusIndicator = (GradientDrawable) status.getBackground();

        if (statusCode == Item.SAFE_STATUS) {
            statusIndicator.setColor(context.getResources().getColor(R.color.safeStatusColor));
        } else if (statusCode == Item.VIBRATING_STATUS) {
            statusIndicator.setColor(context.getResources().getColor(R.color.vibratingStatusColor));
        } else if (statusCode == Item.LOST_STATUS) {
            statusIndicator.setColor(context.getResources().getColor(R.color.lostStatusColor));
        }

        holder.name.setText(currentItem.getName());
        holder.ip.setText(currentItem.getIP());
        holder.dateAdded.setText(DateUtils.getRelativeTimeSpanString(currentItem.getDateAdded().getTime(),
                System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));

        if (isPendingRemoval(position)) {
            holder.deleteDescription.setText("Deleting " + currentItem.getName());
            holder.regularLayout.setVisibility(View.GONE);
            holder.overallLayout.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            holder.swipeLayout.setVisibility(View.VISIBLE);
            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelDeletion(currentItem);
                }
            });
        } else {
            holder.overallLayout.setBackground(context.getResources().getDrawable(R.drawable.rounded_rectangle));
            holder.regularLayout.setVisibility(View.VISIBLE);
            holder.swipeLayout.setVisibility(View.GONE);
        }
    }

    private void cancelDeletion(Item item) {
        Runnable pendingRemovalRunnable = pendingRunnables.get(item.getIP());
        pendingRunnables.remove(item.getIP());
        if (pendingRemovalRunnable != null) {
            handler.removeCallbacks(pendingRemovalRunnable);
        }
        itemsPendingRemoval.remove(item);
        notifyItemChanged(items.indexOf(item));
    }

    public void confirmPendingRemovals() {
        int position;
        for (Item item : itemsPendingRemoval) {
            position = items.indexOf(item);
            if (position != -1) {
                item.setStatus(Item.NO_STATUS);
                items.remove(position);
                notifyItemRemoved(position);
                ((MainActivity) context).showSummary();
            }
        }
        itemsPendingRemoval.clear();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void pendingRemoval(int position) {
        final Item item = items.get(position);
        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item);
            notifyItemChanged(position);
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(item);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(item.getIP(), pendingRemovalRunnable);
        }
    }

    public void remove(Item item) {
        int position = items.indexOf(item);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);
        }
        if (position != -1) {
            item.setStatus(Item.NO_STATUS);
            items.remove(position);
            notifyItemRemoved(position);
            ((MainActivity) context).showSummary();
        }
    }

    public boolean isPendingRemoval(int position) {
        Item item = items.get(position);
        for (Item current : itemsPendingRemoval) {
            if (item.getIP().equals(current.getIP())) {
                return true;
            }
        }
        return false;
    }
}

class ItemViewHolder extends RecyclerView.ViewHolder {

    public FrameLayout overallLayout;
    public LinearLayout regularLayout;
    public LinearLayout swipeLayout;
    public TextView name;
    public TextView ip;
    public TextView dateAdded;
    public TextView deleteDescription;
    public TextView cancel;

    public ItemViewHolder(View view) {
        super(view);

        overallLayout = (FrameLayout) view;

        regularLayout = (LinearLayout) view.findViewById(R.id.layout_info);

        name = (TextView) regularLayout.findViewById(R.id.textview_name);
        ip = (TextView) regularLayout.findViewById(R.id.textview_ip);
        dateAdded = (TextView) regularLayout.findViewById(R.id.textview_date_added);

        swipeLayout = (LinearLayout) view.findViewById(R.id.layout_swipe_item);
        deleteDescription = (TextView) swipeLayout.findViewById(R.id.textview_delete_description);
        cancel = (TextView) swipeLayout.findViewById(R.id.textview_cancel);
    }
}
