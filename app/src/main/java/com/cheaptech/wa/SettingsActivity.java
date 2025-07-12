package com.cheaptech.wa;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.text.InputType;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends Activity {

    private LinearLayout headersLayout;
    private EditText apiKeyInput, phoneInput;
    private Button saveButton, addHeaderButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        apiKeyInput = findViewById(R.id.api_key_input);
        phoneInput = findViewById(R.id.phone_input);
        headersLayout = findViewById(R.id.headers_layout);
        saveButton = findViewById(R.id.save_button);
        addHeaderButton = findViewById(R.id.add_header_button);

        // Load saved values
        apiKeyInput.setText(prefs.getString("api_key", ""));
        phoneInput.setText(prefs.getString("whatsapp_number", ""));

        // Load saved headers
        Map<String, ?> all = prefs.getAll();
        for (String key : all.keySet()) {
            if (key.startsWith("header_")) {
                String headerKey = key.substring(7);
                String value = String.valueOf(all.get(key));
                addHeaderView(headerKey, value);
            }
        }

        addHeaderButton.setOnClickListener(v -> addHeaderView("", ""));

        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("api_key", apiKeyInput.getText().toString());
            editor.putString("whatsapp_number", phoneInput.getText().toString());

            // Save headers
            for (int i = 0; i < headersLayout.getChildCount(); i++) {
                ViewGroup pairLayout = (ViewGroup) headersLayout.getChildAt(i);
                EditText keyInput = (EditText) pairLayout.getChildAt(0);
                EditText valInput = (EditText) pairLayout.getChildAt(1);

                String key = keyInput.getText().toString().trim();
                String val = valInput.getText().toString().trim();
                if (!key.isEmpty()) {
                    editor.putString("header_" + key, val);
                }
            }

            editor.apply();
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        });
    }

    private void addHeaderView(String key, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        EditText keyInput = new EditText(this);
        keyInput.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        keyInput.setHint("Header Key");
        keyInput.setText(key);

        EditText valInput = new EditText(this);
        valInput.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        valInput.setHint("Header Value");
        valInput.setText(value);

        row.addView(keyInput);
        row.addView(valInput);

        headersLayout.addView(row);
    }
}
