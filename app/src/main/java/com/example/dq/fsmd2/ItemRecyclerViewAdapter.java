package com.example.dq.fsmd2;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private Context context;
    private ItemListViewModel itemListViewModel;
    private MonitorDataListViewModel monitorDataListViewModel;

    private List<Item> items;
    private List<Item> itemsPendingRemoval;

    private LayoutInflater inflater;

    private static final int PENDING_REMOVAL_TIMEOUT = 5000;
    private Handler handler = new Handler();
    private HashMap<String, Runnable> pendingRunnables = new HashMap<>();

    public ItemRecyclerViewAdapter(Context context, ArrayList<Item> items, ItemListViewModel itemListViewModel, MonitorDataListViewModel monitorDataListViewModel) {
        this.context = context;
        this.itemListViewModel = itemListViewModel;
        this.monitorDataListViewModel = monitorDataListViewModel;
        this.items = items;
        itemsPendingRemoval = new ArrayList<Item>();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final FrameLayout itemLayout = (FrameLayout) inflater.inflate(R.layout.layout_item, parent, false);
        return new ItemViewHolder(itemLayout, context, itemListViewModel);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final Item currentItem = items.get(position);
        holder.colorFrom = context.getResources().getColor(Item.getStatusColor(currentItem.getStatus()));
        holder.item = currentItem;
        holder.colorTo = context.getResources().getColor(Item.getStatusColor(currentItem.getStatus()));
        holder.observeItem();

        int itemStatus = currentItem.getStatus();

        holder.name.setText(currentItem.getName());
        holder.ip.setText(currentItem.getIp());

        if (itemStatus == Item.NO_STATUS) {
            holder.dateAdded.setVisibility(View.GONE);
            holder.connecting.setVisibility(View.VISIBLE);
        } else {
            holder.connecting.setVisibility(View.GONE);
            holder.dateAdded.setVisibility(View.VISIBLE);
            holder.dateAdded.setText(DateUtils.getRelativeTimeSpanString(currentItem.getDateAdded().getTime(),
                    System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));
        }

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
            holder.overallLayout.setBackground(context.getResources().getDrawable(R.drawable.rectangle));
            holder.overallLayout.setBackgroundColor(Color.WHITE);
            holder.regularLayout.setVisibility(View.VISIBLE);
            holder.swipeLayout.setVisibility(View.GONE);
        }
    }

    private void cancelDeletion(Item item) {
        Runnable pendingRemovalRunnable = pendingRunnables.get(item.getIp());
        pendingRunnables.remove(item.getIp());
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

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void swapItems(int from, int to) {
        Collections.swap(items, from, to);
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
            pendingRunnables.put(item.getIp(), pendingRemovalRunnable);
        }
    }

    private void remove(Item item) {
        int position = items.indexOf(item);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);
        }
        if (position != -1) {
            item.setStatus(Item.NO_STATUS);
            removeItem(item);
            notifyItemRemoved(position);
            ((MainActivity) context).showSummary();
        }
    }

    public boolean isPendingRemoval(int position) {
        Item item = items.get(position);
        for (Item current : itemsPendingRemoval) {
            if (item.getItemID() == current.getItemID()) {
                return true;
            }
        }
        return false;
    }


    private void removeItem(Item item) {
        final int itemID = item.getItemID();
        Thread removeMonitorDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                monitorDataListViewModel.removeMonitorDataFromItem(itemID);
            }
        });
        removeMonitorDataThread.start();
        while (removeMonitorDataThread.isAlive());
        itemListViewModel.removeItem(item);
    }
}

class ItemViewHolder extends RecyclerView.ViewHolder {

    private Context context;
    private ItemListViewModel itemListViewModel;

    public Item item;
    private LiveData<Item> itemLive;
    private Observer<Item> itemObserver;

    public FrameLayout overallLayout;
    public LinearLayout regularLayout;
    public LinearLayout swipeLayout;

    View statusIndicator;
    public TransitionDrawable statusBackground;
    public int colorFrom;
    public int colorTo;
    public TextView name;
    public TextView ip;
    public ProgressBar connecting;
    public TextView dateAdded;
    public TextView deleteDescription;
    public TextView cancel;

    public ItemViewHolder(View view, Context newContext, ItemListViewModel itemListViewModel) {
        super(view);

        this.context = newContext;
        this.itemListViewModel = itemListViewModel;
        this.itemObserver = new Observer<Item>() {
            @Override
            public void onChanged(@Nullable Item item) {
                ColorDrawable[] colors = {new ColorDrawable(colorFrom), new ColorDrawable(colorTo)};
                statusBackground = new TransitionDrawable(colors);
                statusIndicator.setBackground(statusBackground);
                statusBackground.startTransition(300);
                ((MainActivity) context).showSummary();
            }
        };

        overallLayout = (FrameLayout) view;

        regularLayout = view.findViewById(R.id.layout_info);
        statusIndicator = view.findViewById(R.id.view_status);

        name = regularLayout.findViewById(R.id.textview_name);
        ip = regularLayout.findViewById(R.id.textview_ip);
        connecting = regularLayout.findViewById(R.id.progress_bar_connecting);
        dateAdded = regularLayout.findViewById(R.id.textview_date_added);

        swipeLayout = view.findViewById(R.id.layout_swipe_item);
        deleteDescription = swipeLayout.findViewById(R.id.textview_delete_description);
        cancel = swipeLayout.findViewById(R.id.textview_cancel);

        regularLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ItemActivity.class);
                i.putExtra("ITEM", item);
                context.startActivity(i);
            }
        });
    }

    public void observeItem() {
        itemLive = itemListViewModel.getItem(item.getItemID());
        itemLive.observe((MainActivity) context, itemObserver);
    }
}
