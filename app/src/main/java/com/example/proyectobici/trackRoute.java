package com.example.proyectobici;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class trackRoute extends AppCompatActivity {
    ArrayList<LatLng> listLocsToDraw; //Contendra las posiciones de la ruta

    LocalService mService;
    boolean mBound = false;
    private final String TAG = "Tracking Activity";
    //private GoogleMap mMap;
    private Button btn;
    boolean mTracking = false;

    private FrameLayout frmMap;
    private MapFragment mapFragment;
    private FragmentManager fm;
    private MapFragment fragment;
    //ProgressDialog dialog;

    private TextView txtPulso;

    Handler bluetoothIn;
    final int handlerState=0;
    private BluetoothAdapter btAdapter=null;
    private BluetoothSocket btSocket=null;
    private ConnectedThread myConexionBT;
    private StringBuilder DataStringIN = new StringBuilder();

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_route);
        btn = findViewById(R.id.btnTrack);
        txtPulso=(TextView)findViewById(R.id.cmpRoutePulse);

        /*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapp);
        mapFragment.getMapAsync(this);*/

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick();
            }
        });

        fm=getSupportFragmentManager();
        fragment=(MapFragment) fm.findFragmentById(R.id.cmpFrameLayoutMap2);
        if (fragment == null) {
            fragment = new MapFragment((float)-16.40499, (float)-71.50177);
            fm.beginTransaction()
                    .add(R.id.cmpFrameLayoutMap2, fragment)
                    .commit();
        }

        Intent intent=getIntent();
        if (intent.hasExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS)) {
            bluetoothIn = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    if (msg.what == handlerState) {
                        String readMessage = (String) msg.obj;
                        DataStringIN.append(readMessage);

                        int endOfLineIndex = DataStringIN.indexOf("#");

                        if (endOfLineIndex > 0) {
                            String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                            String est = dataInPrint.substring(0, dataInPrint.indexOf("-"));
                            String pulso = dataInPrint.substring(dataInPrint.indexOf("-")+1, endOfLineIndex);
                            txtPulso.setText("Pulso(BPM): " + pulso);
                            Log.i("trackRoute","Pulso - "+pulso);
                            DataStringIN.delete(0, DataStringIN.length());
                        }
                    }
                }
            };

            btAdapter = BluetoothAdapter.getDefaultAdapter();
            VerificarEstadoBT();
        }


        BottomNavigationView navView = findViewById(R.id.nav_view2);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent=getIntent();
        if (intent.hasExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS)) {
            address = intent.getStringExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS);
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            try {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La creacion del Socket fallo", Toast.LENGTH_SHORT).show();
            }

            try {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                }
            }
            myConexionBT = new ConnectedThread(btSocket);
            myConexionBT.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try
        {
            if (btSocket!=null) {
                btSocket.close();
            }
        } catch (IOException e2) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_acciones, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.cmpItemBT){
            Intent it=new Intent(trackRoute.this, DispositivosBT.class);
            startActivity(it);
        }
        return super.onOptionsItemSelected(item);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device)throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    /**
     * Este BroadCast recive la ubicacion por location changed
     */
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LatLng actual = intent.getParcelableExtra("Data");
            int i = Log.i(TAG, "Se ha recivido " + actual);
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
            //mMap.clear();
            fragment.dibujarRuta();
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
        fragment.addUbication(nueva);
    }

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
