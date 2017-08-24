package com.alivc.videochat.demo.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 类的描述: 暂时没有用到
 */
public class TouchableRecyclerView extends RecyclerView {
    private boolean mTouchable = true;

    public TouchableRecyclerView(Context context) {
        super(context);
    }

    public TouchableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mTouchable) {
            switch (MotionEventCompat.getActionMasked(e)) {
                case MotionEvent.ACTION_UP:
                    performClick();
                    break;
            }
            return super.onTouchEvent(e);
        } else {
            return false;
        }
    }

    public void setTouchable(boolean mTouchable) {
        this.mTouchable = mTouchable;
    }
}