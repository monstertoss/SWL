package com.monstertoss.swl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;

public class SettingsActivity extends Activity {

    private static final String TAG = "SettingsActivity";

    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        findViewById(R.id.restartApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Restarting app...");
                startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                System.exit(0);
            }
        });

        applySeekBarStuff((CustomSeekbar) findViewById(R.id.columns), "columns", 5);
        applySeekBarStuff((CustomSeekbar) findViewById(R.id.rows), "rows", 3);
        applySeekBarStuff((CustomSeekbar) findViewById(R.id.margin), "margin", 70);
        applySeekBarStuff((CustomSeekbar) findViewById(R.id.dotSize), "dotSize", 10);
        applySeekBarStuff((CustomSeekbar) findViewById(R.id.lineSize), "lineSize", 8);

        CustomSeekbar colorHue = findViewById(R.id.colorHue);
        int color = settings.getInt("color", Color.parseColor(getString(R.string.defaultColor)));
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        colorHue.setProgress((int) hsv[0] + 1);
        final float saturation = hsv[1];
        final float value = hsv[2];
        colorHue.setColor(color);
        colorHue.setOnProgressChangeListener(new CustomSeekbar.OnProgressChangeListener() {
            @Override
            public void onChanging(CustomSeekbar seekbar, int progress) {
                int realHue = progress - 1;
                float[] hsv = {realHue, saturation, value};
                seekbar.setColor(Color.HSVToColor(hsv));
            }

            @Override
            public void onChanged(CustomSeekbar seekbar, int progress) {
                int realHue = progress - 1;
                float[] hsv = {realHue, saturation, value};
                modify("color", Color.HSVToColor(hsv));
            }
        });
    }

    private void applySeekBarStuff(CustomSeekbar seekBar, final String setting, int defaultValue) {
        seekBar.setProgress(settings.getInt(setting, defaultValue));
        seekBar.setColor(settings.getInt("color", Color.parseColor(getString(R.string.defaultColor))));
        seekBar.setOnProgressChangeListener(new CustomSeekbar.OnProgressChangeListener() {
            @Override
            public void onChanging(CustomSeekbar seekbar, int progress) {
            }

            @Override
            public void onChanged(CustomSeekbar seekbar, int progress) {
                modify(setting, progress);
            }
        });
    }

    private void modify(String name, int value) {
        Log.d(TAG, "Saving " + name + ": " + value);
        settings.edit().putInt(name, value).apply();
    }
}
