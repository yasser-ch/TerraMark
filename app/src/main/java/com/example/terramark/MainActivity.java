package com.example.terramark;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOCATION = 100;

    private TextView tvLatitude, tvLongitude, tvGpsStatus;
    private RequestQueue requestQueue;
    private LocationManager locationManager;

    private final String insertUrl =
            "http://10.0.2.2/map_project/createPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatitude = findViewById(R.id.tv_latitude);
        tvLongitude = findViewById(R.id.tv_longitude);
        tvGpsStatus = findViewById(R.id.tv_gps_status);
        Button btnOpenMap = findViewById(R.id.btn_open_map);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnOpenMap.setOnClickListener(v ->
                startActivity(new Intent(this, OpenMapActivity.class)));

        checkPermissionsAndStart();
    }

    private void checkPermissionsAndStart() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE
                    }, REQ_LOCATION);
        } else {
            startGpsTracking();
        }
    }

    @SuppressLint("MissingPermission")
    private void startGpsTracking() {
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                60000,
                150,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        double alt = location.getAltitude();
                        float acc = location.getAccuracy();

                        tvLatitude.setText("Latitude : " + lat);
                        tvLongitude.setText("Longitude : " + lon);
                        tvGpsStatus.setText("Mise à jour : " +
                                new SimpleDateFormat("HH:mm:ss",
                                        Locale.getDefault()).format(new Date()));

                        String msg = getString(R.string.new_location,
                                lat, lon, alt, acc);
                        Toast.makeText(getApplicationContext(),
                                msg, Toast.LENGTH_LONG).show();

                        sendPositionToServer(lat, lon);
                    }

                    @Override
                    public void onStatusChanged(String provider,
                                                int status, Bundle extras) {
                        String statusText;
                        switch (status) {
                            case LocationProvider.OUT_OF_SERVICE:
                                statusText = "Hors service"; break;
                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                                statusText = "Indisponible"; break;
                            case LocationProvider.AVAILABLE:
                                statusText = "Disponible"; break;
                            default: statusText = "Inconnu";
                        }
                        Toast.makeText(getApplicationContext(),
                                provider + " : " + statusText,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderEnabled(@NonNull String provider) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.waiting_gps),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {
                        tvGpsStatus.setText(getString(R.string.waiting_gps));
                    }
                }
        );
    }

    private void sendPositionToServer(final double lat, final double lon) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                insertUrl,
                response -> tvGpsStatus.setText(
                        getString(R.string.position_sent)),
                error -> tvGpsStatus.setText(
                        getString(R.string.network_error))
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date", sdf.format(new Date()));
                params.put("imei", retrieveDeviceId());
                return params;
            }
        };
        requestQueue.add(request);
    }

    private String retrieveDeviceId() {
        String androidId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null && !androidId.trim().isEmpty())
            return androidId;
        return "UNKNOWN_DEVICE";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGpsTracking();
        } else {
            Toast.makeText(this,
                    getString(R.string.permission_denied),
                    Toast.LENGTH_LONG).show();
        }
    }
}