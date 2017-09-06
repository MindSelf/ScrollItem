package com.example.zhaolexi.scrollitem;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by ZHAOLEXI on 2017/9/3.
 */

public class ScrollItem extends RelativeLayout {

    private Context mCtx;
    private View topLayer;
    private Button delete;
    private Scroller mScroller;
    private int mLastDispatchX;
    private int mLastDispatchY;
    private int mLastInterceptX;
    private int mLastX;

    private boolean isDeletable;
    private boolean isMoving;

    private OnScrollListener scrollListener;

    public void setOnScrollListener(OnScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    public ScrollItem(Context context) {
        this(context, null);
    }

    public ScrollItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCtx = context;
        init();
    }

    private void init() {

        mScroller = new Scroller(mCtx);

        delete = new Button(mCtx);
        delete.setBackgroundColor(Color.RED);
        delete.setText("删除");
        delete.setTextSize(MyUtils.sp2pt(mCtx, 6));
        delete.setTextColor(Color.WHITE);
        delete.setPadding(5, 8, 5, 8);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(0, 0);
        lp.addRule(ALIGN_PARENT_RIGHT);

        addView(delete, lp);

        final ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                topLayer = getChildAt(1);
                delete.getLayoutParams().width = topLayer.getMeasuredWidth() / 5;
                delete.getLayoutParams().height = topLayer.getMeasuredHeight();
                delete.requestLayout();
                //将监听移除在滑动过程中有时topLayer会变为null
//                observer.removeGlobalOnLayoutListener(this);
            }
        });

    }

    public void setDeleteListener(final View.OnClickListener deleteListener) {
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //必须要在删除item之前对scrollItem复原，否则复用的View会保留当前布局
                resetView();
                deleteListener.onClick(v);
            }
        });
    }

    public void resetView() {
        topLayer.scrollBy(-topLayer.getScrollX(), 0);
        isDeletable = false;
    }

    public void resetViewSmoothly() {
        mScroller.startScroll(topLayer.getScrollX(), 0, -topLayer.getScrollX(), 0, 200);
        invalidate();
    }


    //在纵向滑动时才允许外部拦截触摸事件
    //Item滑动时不允许外部拦截触摸事件
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastDispatchX;
                int deltaY = y - mLastDispatchY;
                if (Math.abs(deltaX) < Math.abs(deltaY) && !isMoving){
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            default:
        }
        mLastDispatchX = x;
        mLastDispatchY = y;
        return super.dispatchTouchEvent(ev);
    }

    //滑动距离不超过10时认为是点击事件，ScrollItem不进行拦截
    //在删除按钮范围内滑动也不对事件进行拦截
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x - mLastInterceptX) > 10) {
                    if (isDeletable && isInDeleteRange(ev))
                        return false;
                    return true;
                }
                break;
            default:
        }
        mLastInterceptX = x;
        mLastX = x;
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isInDeleteRange(MotionEvent ev) {
        int left = delete.getLeft();
        int right = delete.getRight();
        int top = delete.getTop();
        int bottom = delete.getBottom();
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        if (x > left && x < right && y > top && y < bottom)
            return true;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;
                //没滑动时只能向左滑
                if (topLayer!=null&&(topLayer.getScrollX() > 0 || deltaX < 0)) {
                    topLayer.scrollBy(-deltaX, 0);
                    if (!isMoving) {
                        isMoving = true;
                        scrollListener.onScrollStateChange(this,OnScrollListener.STATE_STARTMOVING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (topLayer.getScrollX() > delete.getWidth()) {
                    mScroller.startScroll(topLayer.getScrollX(), 0, delete.getWidth() - topLayer.getScrollX(), 0, 500);
                    isDeletable = true;
                    scrollListener.onScrollStateChange(this,OnScrollListener.STATE_DELETABLE);
                } else {
                    mScroller.startScroll(topLayer.getScrollX(), 0, -topLayer.getScrollX(), 0, 500);
                    isDeletable = false;
                    scrollListener.onScrollStateChange(this,OnScrollListener.STATE_UNDELETABLE);
                }
                invalidate();
                isMoving = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                if(topLayer==null)
                    return true;
                mScroller.startScroll(topLayer.getScrollX(), 0, -topLayer.getScrollX(), 0, 500);
                invalidate();
                scrollListener.onScrollStateChange(this,OnScrollListener.STATE_UNDELETABLE);
                isDeletable = false;
                isMoving = false;
                break;
        }
        mLastX = (int) event.getX();
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            topLayer.scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        }
    }


    public interface OnScrollListener {

        int STATE_DELETABLE=0;

        int STATE_UNDELETABLE=1;

        int STATE_STARTMOVING=2;

        void onScrollStateChange(ScrollItem view, int state);
    }

}
