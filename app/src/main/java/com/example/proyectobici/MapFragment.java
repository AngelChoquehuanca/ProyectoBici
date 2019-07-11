package com.example.proyectobici;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener {
//public class MapFragment extends Fragment{

    private GoogleMap mMap;
    float miLat;
    float miLong;

    private LatLng last= new LatLng(miLat, miLong);

    ArrayList<LatLng> listLocsToDraw;


    public MapFragment(float latitud, float longitud){
        miLat=latitud;
        miLong=longitud;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_map, null, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /*mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                googleMap.clear(); //clear old markers

                CameraPosition googlePlex = CameraPosition.builder()
                        .target(new LatLng(37.4219999,-122.0862462))
                        .zoom(10)
                        .bearing(0)
                        .tilt(45)
                        .build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 10000, null);

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(37.4219999, -122.0862462))
                        .title("Spider Man")
                        .icon(bitmapDescriptorFromVector(getActivity(),R.drawable.spider)));

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(37.4629101,-122.2449094))
                        .title("Iron Man")
                        .snippet("His Talent : Plenty of money"));

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(37.3092293,-122.1136845))
                        .title("Captain America"));
            }
        });*/

        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }


        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        LatLng miPos = new LatLng(miLat, miLong);
        //mMap.addMarker(new MarkerOptions().position(miPos).title("Yo"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miPos,18));


    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getContext(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }
    /*public void updateMap(float latitud, float longitud){
        //mMap.clear();
        LatLng miPos = new LatLng(miLat, miLong);
        mMap.addMarker(new MarkerOptions().position(miPos).title("Yo"));
    }*/
    PolylineOptions po = null;
    public void addUbication(LatLng n)
    {
        if(po == null)
        {
            po = new PolylineOptions();
            listLocsToDraw = new ArrayList<>();
            mMap.clear();
        }
        po.add(n);
        listLocsToDraw.add(n); // ubicaciones
        if(last != null)
        {
            //dialog.dismiss();
            Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                    .clickable(false)
                    .add(
                            last,
                            n
                    ));

            last = n;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(n, 20));
    }

    guardarLocalmente gl = new guardarLocalmente();

    public ArrayList<LatLng> terminarRuta(){

        mMap.clear();
        if(po!=null)
            mMap.addPolyline(po);
        gl.guardar(listLocsToDraw);
        po = null;

        return listLocsToDraw;
    }
    public void limpiarMapa(){
        mMap.clear();
    }

    public void dibujarRuta(PolylineOptions p){
        mMap.clear();
        mMap.addPolyline(p);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p.getPoints().get(0), 20));
    }
    public void historial(){
        PolylineOptions p = gl.verFirst();
        dibujarRuta(p);
    }
}
