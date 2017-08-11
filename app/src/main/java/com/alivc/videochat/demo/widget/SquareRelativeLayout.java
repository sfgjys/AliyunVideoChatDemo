package com.alivc.videochat.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class SquareRelativeLayout extends RelativeLayout {

    public SquareRelativeLayout(Context context) {
        super(context);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override protected
    void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int size;
        if (width == 0) {
            size = height;
        } else if (height == 0) {
            size = width;
        } else {
            size = Math.min(width, height);
        }

        int measure_spec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);

        // XXX this is slow
        super.onMeasure(measure_spec, measure_spec);
    }
}
