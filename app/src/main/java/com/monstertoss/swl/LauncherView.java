package com.monstertoss.swl;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LauncherView extends View {
    private Path mPath;

    private ArrayList<Dot> dots;
    private int rows;
    private int columns;

    private int margin;
    private int color;
    private int dotSize;

    private int drawableWidth;

    private Dot startDot;
    private Dot endDot;

    private Paint dotPaint;
    private Paint mPaint;

    private AppStorage storage;
    private PackageManager packageManager;
    private LaunchCallback callback;

    ColorMatrixColorFilter greyScaleFilter;

    public LauncherView(Context context, AppStorage storage, final int columns, final int rows, int margin, int color, int dotSize, int lineSize, LaunchCallback callback) {
        super(context);
        packageManager = context.getPackageManager();
        this.storage = storage;

        dots = new ArrayList<>();
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                dots.add(new Dot(-1, -1, -1, -1));
            }
        }

        this.columns = columns;
        this.rows = rows;
        this.margin = margin;
        this.color = color;
        this.dotSize = dotSize;

        this.callback = callback;

        startDot = new Dot(-1, -1, -1, -1);
        endDot = new Dot(-1, -1, -1, -1);

        dotPaint = new Paint();
        dotPaint.setAntiAlias(true);
        dotPaint.setStyle(Paint.Style.FILL);

        mPath = new Path();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(lineSize);

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        greyScaleFilter = new ColorMatrixColorFilter(matrix);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        drawableWidth = width - (2 * margin);
        int drawableHeight = height - (2 * margin);

        int widthPerColumn = drawableWidth / columns;
        int offsetX = margin + widthPerColumn / 2;

        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                dots.get(x * rows + y).set(x, y, offsetX + (x * widthPerColumn), drawableHeight - (y > 0 ? margin * 2 : 0) - margin - (y * widthPerColumn));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                int col = Color.WHITE;
                // Color:
                //  - startDot
                //  - endDot
                //  - anything between on the vertical axis
                //  - anything between on the horizontal axis
                if ((x == startDot.iX && y == startDot.iY) || (x == endDot.iX && y == endDot.iY) || (startDot.iX == endDot.iX && x == startDot.iX && (startDot.iY < endDot.iY ? y > startDot.iY && y < endDot.iY : y > endDot.iY && y < startDot.iY)) || (startDot.iY == endDot.iY && y == startDot.iY && (startDot.iX < endDot.iX ? x > startDot.iX && x < endDot.iX : x > endDot.iX && x < startDot.iX)))
                    col = color;

                int dotx = dots.get(x * rows + y).x;
                int doty = dots.get(x * rows + y).y;

                if (startDot.isValid() && !(x == startDot.iX && y == startDot.iY)) {
                    SerializableIntent intent = storage.find(startDot.iX, startDot.iY, x, y);
                    if (intent != null) {
                        try {
                            Drawable icon = DrawableCompat.wrap(packageManager.getActivityIcon(intent.getIntent()));
                            icon.mutate();
                            if (x != endDot.iX || y != endDot.iY)
                                icon.setColorFilter(greyScaleFilter);
                            icon.setBounds(dotx - (dotSize * 2), doty - (dotSize * 2), dotx + (dotSize * 2), doty + (dotSize * 2));

                            col = Color.WHITE;
                            dotPaint.setColor(col);
                            canvas.drawCircle(dotx, doty, dotSize * 3, dotPaint);
                            icon.draw(canvas);
                        } catch (PackageManager.NameNotFoundException e) {
                            dotPaint.setColor(col);
                            canvas.drawCircle(dotx, doty, dotSize, dotPaint);
                        }
                    } else {
                        dotPaint.setColor(col);
                        canvas.drawCircle(dotx, doty, dotSize, dotPaint);
                    }
                } else {
                    dotPaint.setColor(col);
                    canvas.drawCircle(dotx, doty, dotSize, dotPaint);
                }
            }
        }

        if (startDot.isValid() && endDot.isValid())
            canvas.drawPath(mPath, mPaint);
    }

    private Dot findClosestDot(final float x, final float y) {
        return Collections.min(dots, new Comparator<Dot>() {
            public int compare(final Dot p1, final Dot p2) {
                double distance1 = Math.hypot(p1.x - x, p1.y - y);
                double distance2 = Math.hypot(p2.x - x, p2.y - y);
                return (int) (distance1 - distance2);
            }
        });
    }

    private boolean touch_start(float x, float y) {
        Dot closestDot = findClosestDot(x, y);
        double distance = Math.hypot(x - closestDot.x, y - closestDot.y);

        if (closestDot.isValid() && distance < drawableWidth / columns) {
            startDot = closestDot;
            return true;
        } else
            return false;
    }

    private boolean touch_move(float x, float y) {
        Dot closestDot = findClosestDot(x, y);
        double distance = Math.hypot(x - closestDot.x, y - closestDot.y);

        boolean changed = false;

        if (closestDot.isValid() && distance < drawableWidth / columns) {
            if (endDot.iX != closestDot.iX || endDot.iY != closestDot.iY) {
                endDot = closestDot;
                changed = true;
            }
        } else {
            if (endDot.isValid()) {
                endDot = new Dot(-1, -1, -1, -1);
                changed = true;
            }
        }

        if (changed) {
            mPath.reset();
            mPath.moveTo(startDot.x, startDot.y);
            mPath.lineTo(endDot.x, endDot.y);
        }
        return changed;
    }

    private boolean touch_up() {
        if (endDot.isValid())
            callback.launch(startDot, endDot);

        startDot = new Dot(-1, -1, -1, -1);
        endDot = new Dot(-1, -1, -1, -1);
        mPath.reset();

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (touch_start(x, y))
                    invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (touch_move(x, y))
                    invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (touch_up())
                    invalidate();
                break;
        }
        return true;
    }

    public interface LaunchCallback {
        void launch(Dot startDot, Dot endDot);
    }
}