package com.cheaptech.wa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch the settings screen on startup
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity
    }
}
