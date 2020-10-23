package com.x.nocrap.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class HackyViewPager extends VerticalViewPager {

    public HackyViewPager(Context context) {
        super(context);
    }

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}