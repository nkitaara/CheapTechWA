package com.cheaptech.wa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = "CheapTechWA";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();

        // Accept only WhatsApp and WhatsApp Business
        if (!"com.whatsapp".equals(pkg) && !"com.whatsapp.w4b".equals(pkg)) return;

        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        String sender = extras.getString("android.title");
        CharSequence messageChar = extras.getCharSequence("android.text");
        String message = messageChar != null ? messageChar.toString() : null;

        if (sender == null || message == null || message.trim().isEmpty()) return;

        String whatsappType = pkg.equals("com.whatsapp") ? "standard" : "business";

        new Thread(() -> {
            try {
                URL url = new URL("https://cheaptech.com.ng/wa_auto.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                // SharedPreferences for custom headers
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                Map<String, ?> allPrefs = prefs.getAll();

                // Add custom header fields (prefixed with "header_")
                for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith("header_")) {
                        String headerName = key.substring(7); // Remove "header_" prefix
                        String value = String.valueOf(entry.getValue());
                        conn.setRequestProperty(headerName, value);
                    }
                }

                // Add additional fixed headers
                if (allPrefs.containsKey("api_key")) {
                    conn.setRequestProperty("api_key", String.valueOf(allPrefs.get("api_key")));
                }

                if (allPrefs.containsKey("whatsapp_number")) {
                    conn.setRequestProperty("whatsapp_number", String.valueOf(allPrefs.get("whatsapp_number")));
                }

                // Include whatsapp_type in headers
                conn.setRequestProperty("whatsapp_type", whatsappType);

                // Create JSON body
                JSONObject payload = new JSONObject();
                payload.put("sender", sender);
                payload.put("message", message);

                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                Log.i(TAG, "Webhook response code: " + responseCode);

            } catch (Exception e) {
                Log.e(TAG, "Error posting to webhook", e);
            }
        }).start();
    }
}
