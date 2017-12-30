package com.monstertoss.swl;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class AppStorage {

    private static final String TAG = "AppStorage";

    private SerializableIntent[][][][] storage;

    private SharedPreferences preferences;

    public AppStorage(Context context, int width, int height) {
        preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        // Load storage from preferences
        storage = new SerializableIntent[width][height][width][height];

        Log.d(TAG, "Loading apps...");
        int i = 0;

        try {
            JSONObject layer1 = new JSONObject(preferences.getString("storage", "{}"));
            Iterator<String> keys1 = layer1.keys();
            while (keys1.hasNext()) {
                String stringKey1 = keys1.next();
                int key1 = Integer.parseInt(stringKey1);

                if (key1 >= storage.length)
                    return;

                JSONObject layer2 = layer1.getJSONObject(stringKey1);
                Iterator<String> keys2 = layer2.keys();
                while (keys2.hasNext()) {
                    String stringKey2 = keys2.next();
                    int key2 = Integer.parseInt(stringKey2);

                    if (key2 >= storage[key1].length)
                        return;

                    JSONObject layer3 = layer2.getJSONObject(stringKey2);
                    Iterator<String> keys3 = layer3.keys();
                    while (keys3.hasNext()) {
                        String stringKey3 = keys3.next();
                        int key3 = Integer.parseInt(stringKey3);

                        if (key3 >= storage[key1][key2].length)
                            return;

                        JSONObject layer4 = layer3.getJSONObject(stringKey3);
                        Iterator<String> keys4 = layer4.keys();
                        while (keys4.hasNext()) {
                            String stringKey4 = keys4.next();
                            int key4 = Integer.parseInt(stringKey4);

                            if (key4 >= storage[key1][key2][key3].length)
                                return;

                            JSONObject object = layer4.getJSONObject(stringKey4);
                            storage[key1][key2][key3][key4] = new SerializableIntent(object.getString("packageName"), object.getString("name"));
                            i++;
                        }
                    }
                }
            }
        } catch (JSONException e) {
        }

        Log.d(TAG, "  ... loaded " + i + " apps");
    }

    public void set(int startX, int startY, int endX, int endY, SerializableIntent intent) {
        if (startX < 0 || startY < 0 || endX < 0 || endY < 0)
            return;

        storage[startX][startY][endX][endY] = intent;
        serializeAndStore();
    }

    public void delete(int startX, int startY, int endX, int endY) {
        if (startX < 0 || startY < 0 || endX < 0 || endY < 0)
            return;

        storage[startX][startY][endX][endY] = null;
        serializeAndStore();
    }

    public SerializableIntent find(int startX, int startY, int endX, int endY) {
        if (startX < 0 || startY < 0 || endX < 0 || endY < 0)
            return null;
        return storage[startX][startY][endX][endY];
    }

    private void serializeAndStore() {
        Log.d(TAG, "Saving apps...");
        int i = 0;

        try {
            JSONObject layer1 = new JSONObject();
            for (int a = 0; a < storage.length; a++) {
                JSONObject layer2 = null;
                for (int b = 0; b < storage[a].length; b++) {
                    JSONObject layer3 = null;
                    for (int c = 0; c < storage[a][b].length; c++) {
                        JSONObject layer4 = null;
                        for (int d = 0; d < storage[a][b][c].length; d++) {
                            if (storage[a][b][c][d] != null) {
                                if (layer2 == null) {
                                    layer2 = new JSONObject();
                                    layer1.put("" + a, layer2);
                                }

                                if (layer3 == null) {
                                    layer3 = new JSONObject();
                                    layer2.put("" + b, layer3);
                                }

                                if (layer4 == null) {
                                    layer4 = new JSONObject();
                                    layer3.put("" + c, layer4);
                                }

                                JSONObject object = new JSONObject();
                                object.put("packageName", storage[a][b][c][d].packageName);
                                object.put("name", storage[a][b][c][d].name);

                                layer4.put("" + d, object);
                                i++;
                            }
                        }
                    }
                }
            }
            preferences.edit().putString("storage", layer1.toString()).apply();
            Log.d(TAG, "  ... saved " + i + " apps");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
