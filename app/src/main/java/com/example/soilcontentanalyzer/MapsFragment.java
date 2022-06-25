package com.example.soilcontentanalyzer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.soilcontentanalyzer.helper.MapHelper;
import com.example.soilcontentanalyzer.helper.MapTypes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsFragment extends Fragment {

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

            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
//                Toast.makeText(this, R.string.error_permission_map, Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(getActivity(), new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION },
                        1);
            }
//            LatLng colombo = new LatLng(6.9271, 79.8612);
//            googleMap.addMarker(new MarkerOptions().position(colombo).title("Colombo"));
//            LatLng kandy = new LatLng(7.2906, 80.6337);
//            googleMap.addMarker(new MarkerOptions().position(kandy).title("Kandy"));
//            LatLng galle = new LatLng(6.0329, 80.2168);
//            googleMap.addMarker(new MarkerOptions().position(galle).title("Galle"));
//            googleMap.moveCamera(CameraUpdateFactory.newLatLng(colombo));
            MapHelper mapHelper = new MapHelper(googleMap);

            ArrayList<Marker> markers = new ArrayList<>();
            //To add a marker
//            Marker m1 = mapHelper.addMarker(6.9271, 79.8612,"Colombo","colomboSniffet",true);
//            markers.add(m1);
            Marker m2 = mapHelper.addMarker(7.2906, 80.2168,"Kandy","kandySniffet",true);
            markers.add(m2);
            Marker m1 = mapHelper.addMarker(7.2907, 80.2169,"Place1","place1Sniffet",true);
            markers.add(m1);
//            Marker m3 = mapHelper.addMarker(6.0329, 80.2168,"Galle","galleSniffet",true);
//            markers.add(m3);


            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            // I suppressed the missing-permission warning because this wouldn't be executed in my
            // case without location services being enabled
            @SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            double userLat = lastKnownLocation.getLatitude();
            double userLong = lastKnownLocation.getLongitude();
            Marker m3 = mapHelper.addMarker(userLat, userLong,"My Location","Nuwan",false);
            markers.add(m3);
            googleMap.setMyLocationEnabled(true);

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}