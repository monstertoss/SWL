package com.monstertoss.swl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends Activity implements LauncherView.LaunchCallback {

    private static final String TAG = "HomeActivity";
    private static final int REQUEST_CODE_ADDING = 1;

    private SharedPreferences settings;

    private AppStorage storage;

    private Mode mode;

    private int columns;
    private int rows;
    private int margin;
    private int color;
    private int dotSize;
    private int lineSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        populateSettings();

        mode = Mode.DEFAULT;

        storage = new AppStorage(this, columns, rows);

        setContentView(new LauncherView(this, storage, columns, rows, margin, color, dotSize, lineSize, this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(uiOptions);
    }

    private void populateSettings() {
        SharedPreferences.Editor editor = settings.edit();

        if (!settings.contains("columns"))
            editor.putInt("columns", 5);
        columns = settings.getInt("columns", 5);

        if (!settings.contains("rows"))
            editor.putInt("rows", 3);
        rows = settings.getInt("rows", 3);

        if (!settings.contains("margin"))
            editor.putInt("margin", 70);
        margin = settings.getInt("margin", 70);

        if (!settings.contains("color"))
            editor.putInt("color", Color.parseColor(getString(R.string.defaultColor)));
        color = settings.getInt("color", Color.parseColor(getString(R.string.defaultColor)));

        if (!settings.contains("dotSize"))
            editor.putInt("dotSize", 10);
        dotSize = settings.getInt("dotSize", 10);

        if (!settings.contains("lineSize"))
            editor.putInt("lineSize", 8);
        lineSize = settings.getInt("lineSize", 8);

        editor.apply();
    }

    private void setMode(Mode mode) {
        int string;
        switch (mode) {
            case DEFAULT:
                string = R.string.mode_default;
                break;

            case ADDING:
                string = R.string.mode_adding;
                break;

            case REMOVING:
                string = R.string.mode_removing;
                break;

            default:
                string = R.string.mode_default;
        }
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Switching mode: " + mode.name());
        this.mode = mode;
    }

    public void launch(Dot start, Dot end) {
        Log.d(TAG, "Launching: " + start + " => " + end);

        if (start.iX == 0 && start.iY == rows - 1 && end.iX == columns - 1 && end.iY == rows - 1) { // Top left => Top right.
            if (mode == Mode.ADDING)
                setMode(Mode.DEFAULT);
            else
                setMode(Mode.ADDING);

            return;
        }

        if (start.iX == columns - 1 && start.iY == rows - 1 && end.iX == 0 && end.iY == rows - 1) { // Top right => Top left.
            if (mode == Mode.REMOVING)
                setMode(Mode.DEFAULT);
            else
                setMode(Mode.REMOVING);

            return;
        }

        if (columns % 2 == 0) {
            int xCenter = columns / 2;
            if (start.iY == 0 && end.iY == rows - 1 && ((start.iX == xCenter && end.iX == xCenter) || (start.iX == xCenter - 1 && end.iX == xCenter - 1))) {
                startActivity(new Intent(this, SettingsActivity.class).putExtra("mode", mode));
                return;
            }

            if (start.iY == rows - 1 && end.iY == 0 && ((start.iX == xCenter && end.iX == xCenter) || (start.iX == xCenter - 1 && end.iX == xCenter - 1))) {
                startActivity(new Intent(this, ListActivity.class).putExtra("mode", mode));
                return;
            }
        } else {
            int xCenter = (columns - 1) / 2;
            if (start.iY == 0 && end.iY == rows - 1 && start.iX == xCenter && end.iX == xCenter) {
                startActivity(new Intent(this, SettingsActivity.class).putExtra("mode", mode));
                return;
            }

            if (start.iY == rows - 1 && end.iY == 0 && start.iX == xCenter && end.iX == xCenter) {
                startActivity(new Intent(this, ListActivity.class).putExtra("mode", mode));
                return;
            }
        }

        if (start.iX == end.iX && start.iY == end.iY) {
            if (start.iY == 0) // Ignore if not in quick bar
                handle(start.iX, 0, start.iX, 0);
            return;
        }

        handle(start.iX, start.iY, end.iX, end.iY);
    }

    private void handle(int startX, int startY, int endX, int endY) {
        switch (mode) {
            case DEFAULT:
                SerializableIntent intent = storage.find(startX, startY, endX, endY);
                if (intent == null)
                    return;
                sendIntent(intent);
                break;

            case ADDING:
                startActivityForResult(new Intent(this, ListActivity.class).putExtra("mode", mode).putExtra("startX", startX).putExtra("startY", startY).putExtra("endX", endX).putExtra("endY", endY), REQUEST_CODE_ADDING);
                break;

            case REMOVING:
                storage.delete(startX, startY, endX, endY);
                break;
        }
    }

    private void sendIntent(SerializableIntent intent) {
        Log.d(TAG, "Launching: " + intent.packageName);
        startActivity(intent.getIntent());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ADDING && resultCode == RESULT_OK && data.getSerializableExtra("mode") == Mode.ADDING) {
            storage.set(data.getIntExtra("startX", -1), data.getIntExtra("startY", -1), data.getIntExtra("endX", -1), data.getIntExtra("endY", -1), new SerializableIntent(data.getStringExtra("packageName"), data.getStringExtra("name")));
        }
    }
}
