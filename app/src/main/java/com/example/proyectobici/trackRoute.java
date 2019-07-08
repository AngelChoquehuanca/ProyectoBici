package com.example.proyectobici;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class trackRoute extends AppCompatActivity implements OnMapReadyCallback {
//{
    ArrayList<LatLng> listLocsToDraw; //Contendra las posiciones de la ruta
    private LatLng last= new LatLng(-35.016, 143.321);

    LocalService mService;
    boolean mBound = false;
    private final String TAG = "Tracking Activity";
    private GoogleMap mMap;
    private Button btn;
    boolean mTracking = false;

    private FrameLayout frmMap;
    private MapFragment mapFragment;
    private FragmentManager fm;
    private MapFragment fragment;
    //ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_route);
        btn = findViewById(R.id.btnTrack);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapp);
        mapFragment.getMapAsync(this);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick();
            }
        });

        /*fm=getSupportFragmentManager();
        fragment=(MapFragment) fm.findFragmentById(R.id.cmpFrameLayoutMap2);
        if (fragment == null) {
            fragment = new MapFragment((float)-16.40499, (float)-71.50177);
            fm.beginTransaction()
                    .add(R.id.cmpFrameLayoutMap2, fragment)
                    .commit();
        }*/




        BottomNavigationView navView = findViewById(R.id.nav_view2);
    }


    /**
     * Este BroadCast recive la ubicacion por location changed
     */
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LatLng actual = intent.getParcelableExtra("Data");
            Log.i(TAG, "Se ha recivido "+ actual.latitude);
            drwMarker(actual);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        //solicita permisos
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        // Bind to LocalService
        Intent intent = new Intent(this, LocalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        unregisterReceiver(broadcastReceiver);
    }

    /** Called when a button is clicked (the button in the layout file attaches to
     * this method with the android:onClick attribute) */
    public void onButtonClick() {
        Log.i(TAG, "boton presionado "+mService);


        if (!mBound) //Veridica si existe el servicio
            return;
        if (!checkLocationPermission()) { //verifica los permisos
            Log.e(TAG, "No hay permisos "+mService);
            return;
        }

        if(!mTracking)
        {
            btn.setText("STOP TRACKING");
            mService.startTracking();
            mTracking = true;
            listLocsToDraw = new ArrayList<>();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("locationActual");
            registerReceiver(broadcastReceiver, intentFilter);

            //dialog = ProgressDialog.show(this, "Loading", "Please wait...", true);

        }
        else{
            btn.setText("START");
            mTracking = false;
            mService.stopTracking();
            mMap.clear();
            unregisterReceiver(broadcastReceiver);
        }

    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Servicio conectado"+mService);
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
            trackRoute.this.mService = binder.getService();
            trackRoute.this.mBound = true;
            listLocsToDraw = new ArrayList<>();
            //Registra Broadcast
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("locationActual");
            registerReceiver(broadcastReceiver, intentFilter);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "Servicio desconectado"+mService);
            mBound = false;
        }
    };

    public void drwMarker(LatLng nueva){
        listLocsToDraw.add(nueva);

        if(last != null)
        {
            //dialog.dismiss();
            Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                    .clickable(false)
                    .add(
                            last,
                            nueva
                    ));
            last = nueva;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nueva, 20));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //mMap.addPolyline(polyline1);
    }
}
