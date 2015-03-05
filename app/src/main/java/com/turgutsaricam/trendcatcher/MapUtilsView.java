package com.turgutsaricam.trendcatcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Turgut on 05.03.2015.
 */
public class MapUtilsView extends View {

    private boolean active = false;
    private boolean drawingNow = false;
    Paint rectPaint;
    RectF drawnRect;

    CommunicatorMapUtilsView comm;

    public MapUtilsView(Context context) {
        super(context);
        init(context);
    }

    public MapUtilsView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public MapUtilsView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        init(context);
    }

    public void init(Context context) {
        comm = (CommunicatorMapUtilsView) context;

        rectPaint = new Paint();
        rectPaint.setColor(Color.rgb(3, 169, 244));
        rectPaint.setStrokeWidth(4f);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setPathEffect(new DashPathEffect(new float[] {10,4}, 0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(active && drawingNow) {
            canvas.drawRect(drawnRect, rectPaint);
        }
    }

    float[] pointSetFirst = new float[2];
    float[] pointSetSecond = new float[2];

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!active) return false;
        boolean handled = false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pointSetFirst[0] = event.getX();
                pointSetFirst[1] = event.getY();

                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                drawingNow = true;
                pointSetSecond[0] = event.getX();
                pointSetSecond[1] = event.getY();

                handled = true;
                break;

            case MotionEvent.ACTION_UP:
                pointSetSecond[0] = event.getX();
                pointSetSecond[1] = event.getY();
                drawnRect = new RectF(pointSetFirst[0], pointSetFirst[1], pointSetSecond[0], pointSetSecond[1]);

                if(drawnRect != null)
                    comm.onScreenCoordsTaken(drawnRect.left, drawnRect.bottom, drawnRect.right, drawnRect.top);
                drawingNow = false;
                handled = true;
                break;
        }

        if(drawingNow)
            drawnRect = new RectF(pointSetFirst[0], pointSetFirst[1], pointSetSecond[0], pointSetSecond[1]);
        invalidate();

        return super.onTouchEvent(event) || handled;
    }

    public void setActive(boolean bool) {
        active = bool;
        invalidate();
    }

    public boolean isActive() {
        return active;
    }

    public interface CommunicatorMapUtilsView {
        public void onScreenCoordsTaken(float leftBottomX, float leftBottomY, float rightTopX, float rightTopY);
    }
}
