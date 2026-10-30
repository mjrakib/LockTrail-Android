package com.locktrail.security;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Looper;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private static final int LOCATION_REQUEST = 42;
    private LinearLayout root;
    private TextView locationValue;
    private TextView statusValue;
    private ToneGenerator toneGenerator;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(28));
        root.setBackgroundColor(Color.rgb(244, 248, 247));
        scroll.addView(root);

        TextView brand = new TextView(this);
        brand.setText("LOCKTRAIL");
        brand.setTextColor(Color.rgb(13, 118, 110));
        brand.setTextSize(13);
        brand.setTypeface(null, Typeface.BOLD);
        root.addView(brand);

        TextView title = new TextView(this);
        title.setText("Mobile Security");
        title.setTextColor(Color.rgb(7, 53, 49));
        title.setTextSize(30);
        title.setTypeface(null, Typeface.BOLD);
        addSpace(8);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Anti-theft prototype · Local device demo");
        subtitle.setTextColor(Color.rgb(83, 104, 101));
        subtitle.setTextSize(14);
        root.addView(subtitle);

        addSpace(22);
        statusValue = addCard("DEVICE STATUS", "Protection setup incomplete", "This prototype does not yet connect to a remote account or dashboard.");
        locationValue = addCard("LAST KNOWN LOCATION", "Not collected yet", "Tap the button below and allow location access.");

        Button locationButton = button("Get last known location");
        locationButton.setOnClickListener(v -> requestLocation());
        root.addView(locationButton);

        Button alarmButton = button("Test local alarm tone");
        alarmButton.setOnClickListener(v -> {
            try {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2500);
                Toast.makeText(this, "Local alarm tone played", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Could not play alarm tone on this device", Toast.LENGTH_LONG).show();
            }
        });
        root.addView(alarmButton);

        addSpace(16);
        TextView limitation = new TextView(this);
        limitation.setText("IMPORTANT LIMITS\n\n• This demo cannot prevent the phone from powering off.\n• It cannot block Factory Reset or Recovery Mode.\n• Location is read locally; it is not uploaded or live-tracked remotely.\n• Remote lock, remote erase, login/2FA, and Device Owner enrollment are not implemented yet.\n\nA production build needs a secure backend and, for stronger policies, supported Android Enterprise / Device Owner enrollment.");
        limitation.setTextColor(Color.rgb(55, 72, 69));
        limitation.setTextSize(14);
        limitation.setPadding(dp(16), dp(16), dp(16), dp(16));
        limitation.setBackgroundColor(Color.rgb(231, 241, 239));
        root.addView(limitation);
        setContentView(scroll);
    }

    private TextView addCard(String heading, String value, String detail) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        root.addView(card, lp);
        TextView h = new TextView(this); h.setText(heading); h.setTextSize(11); h.setTypeface(null, Typeface.BOLD); h.setTextColor(Color.rgb(13,118,110)); card.addView(h);
        TextView v = new TextView(this); v.setText(value); v.setTextSize(18); v.setTypeface(null, Typeface.BOLD); v.setTextColor(Color.rgb(7,53,49)); v.setPadding(0, dp(8), 0, dp(4)); card.addView(v);
        TextView d = new TextView(this); d.setText(detail); d.setTextSize(13); d.setTextColor(Color.rgb(83,104,101)); card.addView(d);
        return v;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(13,118,110)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(10);
        root.addView(b, lp);
        return b;
    }

    private void requestLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        showLastLocation();
    }

    private void showLastLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location best = null;
            for (String provider : lm.getProviders(true)) {
                Location candidate = lm.getLastKnownLocation(provider);
                if (candidate != null && (best == null || candidate.getTime() > best.getTime())) best = candidate;
            }
            if (best == null) {
                locationValue.setText("No cached location available");
                Toast.makeText(this, "No last-known location. Enable location and try again.", Toast.LENGTH_LONG).show();
                return;
            }
            String time = DateFormat.getDateTimeInstance().format(new Date(best.getTime()));
            locationValue.setText(String.format(java.util.Locale.US, "%.6f, %.6f\nAccuracy: %.0f m\nRecorded: %s", best.getLatitude(), best.getLongitude(), best.getAccuracy(), time));
            Toast.makeText(this, "Last known location read on this device only", Toast.LENGTH_LONG).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to read location on this device", Toast.LENGTH_LONG).show();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == LOCATION_REQUEST) {
            boolean granted = false;
            for (int result : results) if (result == PackageManager.PERMISSION_GRANTED) granted = true;
            if (granted) showLastLocation();
            else Toast.makeText(this, "Location permission was not granted", Toast.LENGTH_LONG).show();
        }
    }

    private void addSpace(int dp) {
        View v = new View(this);
        root.addView(v, new LinearLayout.LayoutParams(1, dp(dp)));
    }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    @Override protected void onDestroy() {
        if (toneGenerator != null) toneGenerator.release();
        super.onDestroy();
    }
}
