package com.example.dq.fsmd2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

public abstract class SwipeUtil extends ItemTouchHelper.SimpleCallback {

    private Drawable background;

    private boolean initiated;
    private Context context;

    private int leftColorCode;

    public SwipeUtil(int dragDirs, int swipeDirs, Context context) {
        super(dragDirs, swipeDirs);
        this.context = context;
    }

    private void init() {
        background = new ColorDrawable();
        initiated = true;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public abstract void onSwiped(RecyclerView.ViewHolder viewHolder, int direction);

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return super.getSwipeDirs(recyclerView, viewHolder);
    }


    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;
        if (!initiated) {
            init();
        }

        ((ColorDrawable) background).setColor(getLeftColor());
        background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        background.draw(c);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(48);
        paint.setTextAlign(Paint.Align.CENTER);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    public int getLeftColor() {
        return leftColorCode;
    }

    public void setLeftColor(int leftColorCode) {
        this.leftColorCode = leftColorCode;
    }
}