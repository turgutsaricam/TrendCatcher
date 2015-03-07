package com.turgutsaricam.trendcatcher;

/**
 * Created by Turgut on 07.03.2015.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/** An image view which always remains square with respect to its width. */
final class SquaredImageView extends ImageView {
    public SquaredImageView(Context context) {
        super(context);
//        init(context);
    }

    public SquaredImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init(context);
    }

    Paint rectPaint;
    private void init(Context context) {
        rectPaint = new Paint();
        rectPaint.setColor(Color.argb(180, 2, 229, 255));
        rectPaint.setStrokeWidth(2f);
        rectPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

}
