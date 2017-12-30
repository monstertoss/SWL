package com.monstertoss.swl;

import android.content.ComponentName;
import android.content.Intent;

class SerializableIntent {
    String packageName;
    String name;

    SerializableIntent(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    Intent getIntent() {
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(new ComponentName(packageName, name))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }
}
