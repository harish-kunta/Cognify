package com.gigamind.cognify.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gigamind.cognify.R;

/**
 * A scrim view that darkens the screen but leaves a transparent hole around the
 * highlighted area.
 */
public class HighlightScrimView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF holeRect;

    public HighlightScrimView(Context context) {
        this(context, null);
    }

    public HighlightScrimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(ContextCompat.getColor(context, R.color.scrim));
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void setHole(RectF rect) {
        this.holeRect = rect;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the dimmed overlay
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        if (holeRect != null) {
            Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawRoundRect(holeRect, 16f, 16f, clearPaint);
        }
    }
}
