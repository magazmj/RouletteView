package com.maga.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maga on 2016/11/15.
 *
 * 轮盘视图
 */

public class RouletteView extends ViewGroup {

    private static final String TAG = "RouletteView";

    private static final float WEIGHT = 1.0f;

    private float mCenterX;
    private float mCurDegree;
    private Paint mPaint;
    private Paint mLinePaint;
    private Scroller mScroller;
    private int mRadius;
    private boolean flag;
    private float offsetDegree;
    private float weight = WEIGHT;
    private List<View> mItems;
    private GestureDetector mGestureDetector;
    private boolean isRunning;
    private OnAnimationListener mOnAnimationListener;

    public RouletteView(Context context) {
        super(context);
        initView();
    }

    public RouletteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RouletteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView(){
        setClickable(true);
        setWillNotDraw(false);

        mScroller = new Scroller(getContext(), new FastOutSlowInInterpolator());

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0x66FF0000);
        mPaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(0x6600FF00);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(3);

        mItems = new ArrayList<>();
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){

            float x, y;
            float degree_start;
            float degree_end;

            @Override
            public boolean onDown(MotionEvent e) {
                x = e.getX();
                y = e.getY();
                if(isRunning){
                    mScroller.abortAnimation();
                }
                return super.onDown(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                degree_start = point2Degree(x, y);
                x -= distanceX;
                y -= distanceY;
                degree_end = point2Degree(x, y);
                float delta = degree_end - degree_start;
                mCurDegree += delta;
                requestLayout();
                invalidate();

                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float origin = point2Degree(e1.getX(), e1.getY());
                float target = point2Degree(e2.getX(), e2.getY());
                float delta = target - origin;
                //TODO
                if(e2.getX() > mCenterX && e1.getX() < mCenterX){
                    if(e2.getY() < mCenterX && e1.getY() < mCenterX){
                        delta = target + 360 - origin;
                    }
                }else if(e2.getX() < mCenterX && e1.getX() > mCenterX){
                    if(e2.getY() < mCenterX && e1.getY() < mCenterX){
                        delta = target - 360 - origin;
                    }
                }
                float distance = (float) Math.sqrt(Math.pow(e2.getX() - e1.getX(), 2) + Math.pow(e2.getY() - e1.getY(), 2));
                float time = (float) (distance / Math.sqrt(velocityX * velocityX + velocityY * velocityY));
                float v = delta / (time * weight);

                mScroller.fling(0, 0, (int) v, 0, -10000, +10000, 0, 0);
                isRunning = true;
                lastDegree = mCurDegree;
                if(mOnAnimationListener != null){
                    mOnAnimationListener.onStart();
                }
                invalidate();
                return true;
            }

            private float point2Degree(float x, float y){
                return (float) Math.toDegrees(Math.atan2(mCenterX - x, y - mCenterX) + Math.PI);
            }


        });
    }

    public void setRadius(int radius){
        if(mRadius != radius){
            mRadius = radius;
            requestLayout();
            invalidate();
        }
    }

    public void addItem(View view){
        if(view instanceof TextView){
            ((TextView) view).setText("测试");
            ((TextView) view).setTextColor(0x660000FF);
        }
        mItems.add(view);
        addViewInner(view);

        if(mItems.size() > 0){
            offsetDegree = 180f / mItems.size();
        }
    }

    private void addViewInner(View view){
        flag = true;
        addView(view);
        flag = false;
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        if(!flag) throw new IllegalStateException("can't call addView");
        super.addView(child, index, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mItems.size() == 0) super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        if(widthMode != MeasureSpec.EXACTLY){
            throw new IllegalArgumentException("mode must be EXACTLY");
        }

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mCenterX = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
        if(mRadius == 0 || mRadius > mCenterX){
            mRadius = (int) mCenterX;
        }

        setMeasuredDimension((int)mCenterX * 2, (int)mCenterX * 2);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(mItems.size() < 2) return;
        int cx = 0 , cy = 0;
        for(int i = 0; i < mItems.size(); i++){
            View view = getChildAt(i);
            //TODO 计算cx, cy位置
            view.layout(cx - view.getMeasuredWidth() / 2, cy - view.getMeasuredHeight() / 2, cx + view.getMeasuredWidth() / 2, cy + view.getMeasuredHeight() / 2);
        }
    }

    float lastDegree = 0;

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            float curX = mScroller.getCurrX();
            mCurDegree = lastDegree + curX;
            invalidate();
        } else {
            if(isRunning){
                isRunning = false;
                if(mOnAnimationListener != null){
                    mOnAnimationListener.onStop();
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mItems.size() < 2) return;

        canvas.drawCircle(mCenterX, mCenterX, mRadius, mPaint);

        int num = mItems.size();
        float degree = 360.f / num;
        for(int i = 0; i < num; i++){
            canvas.save();
            canvas.translate(mCenterX, mCenterX);
            canvas.rotate(degree * i + mCurDegree - offsetDegree);
            canvas.drawLine(0, 0, 0, -mRadius, mLinePaint);
            canvas.restore();
        }

        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void setAnimationListener(OnAnimationListener listener){
        this.mOnAnimationListener = listener;
    }

    public interface OnAnimationListener{
        void onStart();
        void onStop();
    }
}
