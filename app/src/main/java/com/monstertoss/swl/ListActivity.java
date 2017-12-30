package com.monstertoss.swl;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListActivity extends Activity {

    private static final String TAG = "ListActivity";

    public class AppDetail {
        AppDetail(ResolveInfo info) {
            label = info.loadLabel(manager).toString();
            packageName = info.activityInfo.packageName;
            name = info.activityInfo.name;
            icon = info.activityInfo.loadIcon(manager);
        }

        String label;
        String packageName;
        String name;
        Drawable icon;
    }

    private PackageManager manager;
    private ArrayList<AppDetail> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        manager = getPackageManager();
        apps = new ArrayList<>();

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
        for (ResolveInfo info : availableActivities) {
            apps.add(new AppDetail(info));
        }
        Collections.sort(apps, new Comparator<AppDetail>() {
            @Override
            public int compare(AppDetail o1, AppDetail o2) {
                return o1.label.compareTo(o2.label);
            }
        });

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this, R.layout.item_list, apps) {
            @Override
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.item_list, null);

                TextView textView = convertView.findViewById(R.id.textView);
                textView.setText(apps.get(position).label);

                ImageView imageView = convertView.findViewById(R.id.imageView);
                imageView.setImageDrawable(apps.get(position).icon);
                return convertView;
            }
        };

        GridView gridView = findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getIntent();
                Mode mode = (Mode) intent.getSerializableExtra("mode");

                switch (mode) {
                    case DEFAULT:
                        Log.d(TAG, "Launching: " + apps.get(position).packageName);
                        startActivity(new SerializableIntent(apps.get(position).packageName, apps.get(position).name).getIntent());
                        break;

                    case ADDING:
                        setResult(RESULT_OK, getIntent().putExtra("packageName", apps.get(position).packageName).putExtra("name", apps.get(position).name));
                        finish();
                        break;
                }
            }
        });
        gridView.setAdapter(adapter);
        gridView.getSelector().setAlpha(0);
    }
}
