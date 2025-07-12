package com.cheaptech.wa;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class NotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();

        // Only process WhatsApp and WhatsApp Business
        if (!pkg.equals("com.whatsapp") && !pkg.equals("com.whatsapp.w4b")) return;

        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        String sender = extras.getString("android.title");
        String message = String.valueOf(extras.getCharSequence("android.text"));

        if (sender == null || message == null) return;

        String whatsappType = pkg.equals("com.whatsapp") ? "standard" : "business";

        new Thread(() -> {
            try {
                URL url = new URL("https://cheaptech.com.ng/wa_auto.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                // Load shared headers from SharedPreferences
                Map<String, ?> prefs = getSharedPreferences("prefs", MODE_PRIVATE).getAll();

                // Add API key and phone number if provided
                if (prefs.containsKey("api_key")) {
                    conn.setRequestProperty("api_key", String.valueOf(prefs.get("api_key")));
                }
                if (prefs.containsKey("whatsapp_number")) {
                    conn.setRequestProperty("whatsapp_number", String.valueOf(prefs.get("whatsapp_number")));
                }

                // Add custom headers
                for (String key : prefs.keySet()) {
                    if (key.startsWith("header_")) {
                        String headerKey = key.substring(7); // Remove "header_" prefix
                        String value = String.valueOf(prefs.get(key));
                        conn.setRequestProperty(headerKey, value);
                    }
                }

                // Add WhatsApp type (standard/business)
                conn.setRequestProperty("whatsapp_type", whatsappType);

                // Prepare JSON body
                JSONObject payload = new JSONObject();
                payload.put("sender", sender);
                payload.put("message", message);

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.i("CheapTechWA", "Webhook response: " + responseCode);

            } catch (Exception e) {
                Log.e("CheapTechWA", "Webhook error: " + e.getMessage());
            }
        }).start();
    }
}
