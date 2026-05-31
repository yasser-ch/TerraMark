package com.example.terramark;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class OpenMapActivity extends AppCompatActivity {

    private MapView osmMap;
    private RequestQueue requestQueue;

    private final String showUrl =
            "http://10.0.2.2/map_project/getPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("terramark_prefs", MODE_PRIVATE));

        setContentView(R.layout.activity_open_map);

        osmMap = findViewById(R.id.osm_map);
        osmMap.setTileSource(TileSourceFactory.MAPNIK);
        osmMap.setBuiltInZoomControls(true);
        osmMap.setMultiTouchControls(true);
        osmMap.getController().setZoom(12.0);
        osmMap.getController().setCenter(
                new GeoPoint(31.6295, -7.9811)); // Marrakech

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        loadMarkersFromServer();
    }

    private void loadMarkersFromServer() {
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.POST,
                showUrl,
                null,
                response -> {
                    try {
                        JSONArray positions =
                                response.getJSONArray("positions");
                        for (int i = 0; i < positions.length(); i++) {
                            JSONObject pos = positions.getJSONObject(i);
                            double lat = pos.getDouble("latitude");
                            double lon = pos.getDouble("longitude");
                            addMarkerToMap(lat, lon, i + 1);
                        }
                        osmMap.invalidate();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
        );
        requestQueue.add(jsonRequest);
    }

    private void addMarkerToMap(double lat, double lon, int index) {
        Marker marker = new Marker(osmMap);
        marker.setPosition(new GeoPoint(lat, lon));
        marker.setTitle("Position " + index);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getScaledMarkerIcon());
        osmMap.getOverlays().add(marker);
    }

    private Drawable getScaledMarkerIcon() {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.marker);
        Bitmap bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return new android.graphics.drawable.BitmapDrawable(
                getResources(), bitmap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        osmMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        osmMap.onPause();
    }
}