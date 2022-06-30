package com.example.soilcontentanalyzer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.soilcontentanalyzer.Model.MeasurementModel;
import com.example.soilcontentanalyzer.helper.MapHelper;
import com.example.soilcontentanalyzer.helper.MapTypes;
import com.example.soilcontentanalyzer.helper.Path;
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

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MapsFragment extends Fragment {

    MapHelper mapHelper;
    GoogleMap gMap;
    Button btn_add_stop;
    Button btn_save;
    Button btn_measure;
    ArrayList<Path> paths = new ArrayList<>();
    Stack<LatLng> previousPoints = new Stack<>();
    LocationManager locationManager;
    String locationProvider;
    public static int locationNo = 1;
    ArrayList<Marker> markers;

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
            @SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            double userLat = lastKnownLocation.getLatitude();
            double userLong = lastKnownLocation.getLongitude();
            previousPoints.push(new LatLng(userLat, userLong));
            Marker m3 = mapHelper.addMarker(userLat, userLong, "My Location", "Nuwan", false);
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
        }
    };

    private void drawPathWithSomeMarkers() {
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
                @SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                double userLat = lastKnownLocation.getLatitude();
                double userLong = lastKnownLocation.getLongitude();
                LatLng latLng1 = previousPoints.pop();
                LatLng latLng2 = new LatLng(userLat, userLong);
                previousPoints.push(latLng2);
                paths.add(new Path(latLng1.latitude, latLng1.longitude, userLat, userLong));
                Polyline line1 = gMap.addPolyline(new PolylineOptions().add(latLng1, latLng2).width(5).color(Color.BLACK));
                Toast.makeText(getContext(), "Added a stop", Toast.LENGTH_SHORT);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:
                Toast.makeText(getContext(), "Saving map", Toast.LENGTH_SHORT);
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
                @SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                double userLat = lastKnownLocation.getLatitude();
                double userLong = lastKnownLocation.getLongitude();
                Marker marker = mapHelper.addMarker(userLat, userLong, "Location " + locationNo, "", false);
                locationNo++;
                markers.add(marker);
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

                MainActivity.SIZE += 1;
                MainActivity.measurementModels.add(new MeasurementModel(MainActivity.SIZE, rand(), rand(), rand()));
            }
        });
        return view;
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

}
