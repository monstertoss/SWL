package com.monstertoss.swl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class CustomSeekbar extends ConstraintLayout {

    public CustomSeekbar(Context context) {
        super(context);
    }

    public CustomSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private SeekBar seekBar;
    private OnProgressChangeListener onProgressChangeListener;

    private void init(Context context, AttributeSet attributeSet) {
        TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.CustomSeekbar);

        View layout = LayoutInflater.from(context).inflate(R.layout.custom_seekbar, this, true);

        ((TextView) layout.findViewById(R.id.description)).setText(attributes.getText(R.styleable.CustomSeekbar_text));

        int max = attributes.getInteger(R.styleable.CustomSeekbar_max, 0);

        seekBar = layout.findViewById(R.id.seekBar);
        seekBar.setMax(max - 1);

        setColor(attributes.getColor(R.styleable.CustomSeekbar_color, Color.WHITE));
        seekBar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
                if (onProgressChangeListener != null)
                    onProgressChangeListener.onChanging(CustomSeekbar.this, progress + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (onProgressChangeListener != null)
                    onProgressChangeListener.onChanged(CustomSeekbar.this, progress + 1);
            }
        });

        ((TextView) layout.findViewById(R.id.legendLeft)).setText("1");
        ((TextView) layout.findViewById(R.id.legendCenter)).setText("" + (max + 1) / 2);
        ((TextView) layout.findViewById(R.id.legendRight)).setText("" + max);

        attributes.recycle();
    }

    public void setColor(int color) {
        seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

    public void setProgress(int progress) {
        seekBar.setProgress(progress - 1);
    }

    public interface OnProgressChangeListener {
        void onChanging(CustomSeekbar seekbar, int progress);

        void onChanged(CustomSeekbar seekbar, int progress);
    }
}
