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

        // Only process WhatsApp and WhatsApp Business
        if (!pkg.equals("com.whatsapp") && !pkg.equals("com.whatsapp.w4b")) return;

        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        String sender = extras.getString("android.title");
        CharSequence messageCharSeq = extras.getCharSequence("android.text");
        String message = messageCharSeq != null ? messageCharSeq.toString() : null;

        if (sender == null || message == null || message.trim().isEmpty()) return;

        String whatsappType = pkg.equals("com.whatsapp") ? "standard" : "business";

        new Thread(() -> {
            try {
                // Prepare connection
                URL url = new URL("https://cheaptech.com.ng/wa_auto.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                // Shared Preferences for custom headers and API key
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                Map<String, ?> allPrefs = prefs.getAll();

                // Inject API key
                if (prefs.contains("api_key")) {
                    conn.setRequestProperty("api_key", String.valueOf(allPrefs.get("api_key")));
                }

                // Inject WhatsApp number (if available)
                if (prefs.contains("whatsapp_number")) {
                    conn.setRequestProperty("whatsapp_number", String.valueOf(allPrefs.get("whatsapp_number")));
                }

                // Add custom headers (prefixed with "header_")
                for (String key : allPrefs.keySet()) {
                    if (key.startsWith("header_")) {
                        String headerKey = key.substring(7); // Remove "header_" prefix
                        String headerValue = String.valueOf(allPrefs.get(key));
                        conn.setRequestProperty(headerKey, headerValue);
                    }
                }

                // Add WhatsApp type
                conn.setRequestProperty("whatsapp_type", whatsappType);

                // Construct JSON body
                JSONObject payload = new JSONObject();
                payload.put("sender", sender);
                payload.put("message", message);

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.i(TAG, "Webhook response: " + responseCode);

            } catch (Exception e) {
                Log.e(TAG, "Error sending webhook: ", e);
            }
        }).start();
    }
}
