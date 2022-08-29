package com.example.soilcontentanalyzer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.soilcontentanalyzer.Model.Measurement;
import com.example.soilcontentanalyzer.helper.MapHelper;
import com.example.soilcontentanalyzer.helper.MapTypes;
import com.example.soilcontentanalyzer.Model.Path;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsFragment extends Fragment implements LocationListener {

    private static final String TAG = "MAPSFragment";
    public static MapHelper mapHelper;
    static GoogleMap gMap;
    Button btn_add_stop;
    Button btn_save;
    Button btn_measure;
    LocationManager locationManager;
    String locationProvider;
    public static int locationNo = 1;
    static ArrayList<Marker> markers;
    LoadingDialog loadDialog;
    public static double latitude;
    public static double longitude;
    public Criteria criteria;
    public String bestProvider;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {

            //            https://github.com/girishnair12345/Google-Maps-V2-library
            //            https://stackoverflow.com/questions/16416041/zoom-to-fit-all-markers-on-map-google-maps-v2
            gMap = googleMap;
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                //                Toast.makeText(this, R.string.error_permission_map, Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
            mapHelper = new MapHelper(googleMap);

            markers = new ArrayList<>();

            //For testing
            drawPathWithSomeMarkers();
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            locationProvider = LocationManager.NETWORK_PROVIDER;
            // I suppressed the missing-permission warning because this wouldn't be executed in my
            // case without location services being enabled
//            @SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
//            double userLat = lastKnownLocation.getLatitude();
//            double userLong = lastKnownLocation.getLongitude();
            getCurrentLocation();
            MainActivity.previousPoints.push(new LatLng(latitude, longitude));
//            Marker m3 = mapHelper.addMarker(latitude, longitude, "My Location", "Nuwan", false);
            //            markers.add(m3);
            //            googleMap.setMyLocationEnabled(true);

            //Set the map type
            mapHelper.setMapType(MapTypes.NORMAL);

            //To enable the zoom controls (+/- buttons)
            mapHelper.setZoomControlsEnabled(true);

            //Calculate the markers to get their position
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            for (Marker m : markers) {
                b.include(m.getPosition());
            }
            LatLngBounds bounds = b.build();
            //Change the padding as per needed
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.15);
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            googleMap.animateCamera(cu);
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    LinearLayout info = new LinearLayout(getContext());
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(getContext());
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(getContext());
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });
        }
    };

    private void drawPathWithSomeMarkers() {
        //TODO : remove after testing real path drawing

        // To draw a route path
        LatLng l1 = new LatLng(7.2906f, 80.2168f);
        LatLng l2 = new LatLng(7.29071f, 80.2168f);
        LatLng l3 = new LatLng(7.2907f, 80.2169f);
        LatLng l4 = new LatLng(7.29057f, 80.216934f);
        Polyline line1 = gMap.addPolyline(new PolylineOptions().add(l1, l2).width(5).color(Color.BLACK));
        Polyline line2 = gMap.addPolyline(new PolylineOptions().add(l2, l3).width(5).color(Color.BLACK));
        Polyline line3 = gMap.addPolyline(new PolylineOptions().add(l3, l4).width(5).color(Color.BLACK));
        Polyline line4 = gMap.addPolyline(new PolylineOptions().add(l4, l1).width(5).color(Color.BLACK));


        // To add a marker
        Marker m1 = mapHelper.addMarker(7.2906, 80.2168, "Location 1", "", true);
        markers.add(m1);
        Marker m2 = mapHelper.addMarker(7.29067, 80.21683, "Location 2", "", true);
        markers.add(m2);

        if (! MainActivity.CHANGED) {
            MainActivity.paths.add(new Path(l1.latitude,l1.longitude,l2.latitude,l2.longitude));
            MainActivity.paths.add(new Path(l2.latitude,l2.longitude,l3.latitude,l3.longitude));
            MainActivity.paths.add(new Path(l3.latitude,l3.longitude,l4.latitude,l4.longitude));
            MainActivity.paths.add(new Path(l4.latitude,l4.longitude,l1.latitude,l1.longitude));
            MainActivity.CHANGED = true;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocationManager service = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        Toast.makeText(getContext(), "OnCreate", Toast.LENGTH_SHORT);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Toast.makeText(getContext(), "OnCreateView", Toast.LENGTH_SHORT);
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        btn_add_stop = view.findViewById(R.id.btn_add_stop);
        btn_save = view.findViewById(R.id.btn_save);
        btn_measure = view.findViewById(R.id.btn_measure);

        btn_add_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationManager == null) {
                    locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                    locationProvider = LocationManager.NETWORK_PROVIDER;
                }
                getCurrentLocation();
//                @SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
//                double userLat = lastKnownLocation.getLatitude();
//                double userLong = lastKnownLocation.getLongitude();
                LatLng latLng1 = MainActivity.previousPoints.pop();
                LatLng latLng2 = new LatLng(latitude, longitude);
                MainActivity.previousPoints.push(latLng2);
                MainActivity.paths.add(new Path(latLng1.latitude, latLng1.longitude, latitude, longitude));
                MainActivity.CHANGED = true;
                Polyline line1 = gMap.addPolyline(new PolylineOptions().add(latLng1, latLng2).width(5).color(Color.BLACK));
                Toast.makeText(getContext(), "Added a stop", Toast.LENGTH_SHORT);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Saving map", Toast.LENGTH_SHORT);
                if (!MainActivity.CHANGED) {
                    Toast.makeText(getContext(), "First update the Map", Toast.LENGTH_SHORT).show();
                } else {
                    loadDialog = new LoadingDialog(getActivity());
                    loadDialog.startLoadingDialog();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            doPostRequest();
                        }
                    }).start();
                }
            }
        });

        btn_measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:
                //mark location
                //request from device
                //receive NPM values
                //view them

                //mark location
                if (locationManager == null) {
                    locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                    locationProvider = LocationManager.NETWORK_PROVIDER;
                }
                getCurrentLocation();

                //TODO : get values from device and replace below
                String cmdText = "<request>";
                // Send command to Arduino board
                MainActivity.connectedThread.write(cmdText);

//                double value_N = rand();
//                double value_P = rand();
//                double value_K = rand();
//                MainActivity.measurements.add(new Measurement(MainActivity.SIZE, value_N, value_P, value_K, latitude, longitude));
//                DecimalFormat df = new DecimalFormat("#.0");
//                Marker marker = mapHelper.addMarker(latitude, longitude, "Location " + locationNo, String.format("N = %smg/kg\nP = %smg/kg\nK = %smg/kg", df.format(value_N), df.format(value_P), df.format(value_K)), false);
//                locationNo++;
//                markers.add(marker);
                LatLngBounds.Builder b = new LatLngBounds.Builder();
                for (Marker m : markers) {
                    b.include(m.getPosition());
                }
                LatLngBounds bounds = b.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.15);
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                gMap.animateCamera(cu);
                Toast.makeText(getContext(), "Measuring NPK", Toast.LENGTH_SHORT);

//                MainActivity.SIZE += 1;
//                MainActivity.CHANGED = true;
            }
        });
        return view;
    }

    private void doPostRequest() {
        Log.d("Okhttp3:", "doPostRequest function called");
        SharedPreferences pref = getContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String baseURL = pref.getString("baseURL", null);
        String url = baseURL + "/map/add";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // connect timeout
                .writeTimeout(30, TimeUnit.SECONDS) // write timeout
                .readTimeout(30, TimeUnit.SECONDS) // read timeout
                .build();

        MediaType JSON = MediaType.parse("application/json;charset=utf-8");

        JSONArray paths = new JSONArray();
        Log.e(TAG, "doPostRequest: Uploading Measurements : " + MainActivity.measurements);
        for (Path path : MainActivity.paths) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("latitudeP1", path.getLatitudeP1());
                jo.put("longitudeP1", path.getLongitudeP1());
                jo.put("latitudeP2", path.getLatitudeP2());
                jo.put("longitudeP2", path.getLongitudeP2());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            paths.put(jo);
        }

        JSONArray measurements = new JSONArray();
        for (Measurement measurement : MainActivity.measurements) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("location", measurement.getLocation());
                jo.put("n", measurement.getN());
                jo.put("p", measurement.getP());
                jo.put("k", measurement.getK());
                jo.put("latitude", measurement.getLatitude());
                jo.put("longitude", measurement.getLongitude());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            measurements.put(jo);
        }

        JSONObject actualData = new JSONObject();
        try {
            actualData.put("paths", paths);
            actualData.put("measurements", measurements);
        } catch (JSONException e) {
            Log.d("Okhttp3:", "JSON Exception");
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON, actualData.toString());
        Log.d("Okhttp3:", "Requestbody created");
        Log.d("Okhttp3:", "body = \n" + body.toString());
        Log.d("Okhttp3:", "actualData = \n" + actualData.toString());
        String jwt = pref.getString("jwt", null);
        Log.d("Okhttp3:", "jwt = " + jwt);
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + jwt)
                .url(url)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.d("Okhttp3:", "request done, got the response");
            Log.d("Okhttp3:", response.body().string());


            final String toast_message;
            loadDialog.dismissDialog();
            if (response.code() == 200) {
                startActivity(new Intent(getContext(), PopUpSubmission.class));
            } else {
                toast_message = "Something Went Wrong";
                if (getContext() != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getContext(), toast_message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }


        } catch (IOException e) {
            loadDialog.dismissDialog();
            Log.d("Okhttp3:", "IOEXCEPTION while request");
            String toast_message = "Something Went Wrong";
            Toast.makeText(getContext(), toast_message, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    private double rand() {
        double rangeMin = 0, rangeMax = 20;
        Random r = new Random();
        double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        return randomValue;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toast.makeText(getContext(), "OnViewCreated", Toast.LENGTH_SHORT);
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    public static boolean isLocationEnabled(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    protected void getCurrentLocation() {
//        https://stackoverflow.com/questions/32290045/error-invoke-virtual-method-double-android-location-location-getlatitude-on
        if (isLocationEnabled(getContext())) {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Toast.makeText(getContext(), "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
            }
            else{
                //This is what you need:
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
            }
        }
        else
        {
            //prompt user to enable location....
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
    }

    @SuppressLint("MissingPermission")
    protected void getCurrentLocation_new() {
//        https://stackoverflow.com/questions/32290045/error-invoke-virtual-method-double-android-location-location-getlatitude-on
        if (isLocationEnabled(getContext())) {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Toast.makeText(getContext(), "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
            }
            else{
                //This is what you need:
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
            }
        }
        else
        {
            //prompt user to enable location....
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //Hey, a non null location! Sweet!

        //remove location callback:
        locationManager.removeUpdates(this);

        //open the map:
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Toast.makeText(getContext(), "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {

    }

    @Override
    public void onFlushComplete(int requestCode) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}
