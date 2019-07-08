package com.example.proyectobici;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.location.Geocoder;
import java.util.Locale;
import android.location.Address;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class RouteActivity extends AppCompatActivity implements LocationListener{
    private FrameLayout frmMap;
    private MapFragment mapFragment;
    private FragmentManager fm;
    private MapFragment fragment;

    private TextView mTextMessage;
    private TextView txtPulso;
    private TextView txtBT;

    Handler bluetoothIn;
    final int handlerState=0;
    private BluetoothAdapter btAdapter=null;
    private BluetoothSocket btSocket=null;
    private ConnectedThread myConexionBT;
    private StringBuilder DataStringIN = new StringBuilder();

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address=null;

    LocationManager locationManager;
    private TextView txtLatitud;
    private TextView txtLongitud;
    private Location location;
    private final int REQUEST_LOCATION = 200;
    private static final String TAG = "RouteActivity";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_route:
                    mTextMessage.setText(R.string.title_route);
                    return true;
                case R.id.navigation_history:
                    mTextMessage.setText(R.string.title_history);
                    return true;
                case R.id.navigation_other:
                    mTextMessage.setText(R.string.title_other);
                    return true;
                case R.id.navigation_store:
                    mTextMessage.setText(R.string.title_store);
                    return true;
            }
            return false;
        }
    };

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);


        /*mapFragment=new MapFragment();
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.cmpFrameLayoutMap,mapFragment);
        fragmentTransaction.commit();*/

        fm=getSupportFragmentManager();
        fragment=(MapFragment) fm.findFragmentById(R.id.cmpFrameLayoutMap);
        if (fragment == null) {
            fragment = new MapFragment((float)-16.40499, (float)-71.50177);
            fm.beginTransaction()
                    .add(R.id.cmpFrameLayoutMap, fragment)
                    .commit();
        }


        BottomNavigationView navView = findViewById(R.id.nav_view);
        mTextMessage = findViewById(R.id.message);
        txtPulso=(TextView)findViewById(R.id.cmpTxtPulso);
        txtBT=(TextView)findViewById(R.id.cmpTxtBT);
        txtLatitud=(TextView)findViewById(R.id.cmpTxtLatitud);
        txtLongitud=(TextView)findViewById(R.id.cmpTxtLongitud);




        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RouteActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (location != null) {
                txtLatitud.setText(String.valueOf(location.getLatitude()));
                txtLongitud.setText(String.valueOf(location.getLongitude()));
                getAddressFromLocation(location, getApplicationContext(), new GeoCoderHandler());
            }
        } else {
            showGPSDisabledAlertToUser();
        }




        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

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
                            txtPulso.setText("Estado: " + est);//<-<- PARTE A MODIFICAR >->->
                            txtBT.setText("Pulso(BPM): " + pulso);
                            DataStringIN.delete(0, DataStringIN.length());
                        }
                    }
                }
            };

            btAdapter = BluetoothAdapter.getDefaultAdapter();
            VerificarEstadoBT();
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
            Intent it=new Intent(RouteActivity.this, DispositivosBT.class);
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

    @Override
    public void onLocationChanged(Location location) {
        txtLatitud.setText(String.valueOf(location.getLatitude()));
        txtLongitud.setText(String.valueOf(location.getLongitude()));
        getAddressFromLocation(location, getApplicationContext(), new GeoCoderHandler());

        /*fragment = new MapFragment((float)location.getLatitude(), (float)location.getLongitude());
        fm.beginTransaction()
                .add(R.id.cmpFrameLayoutMap, fragment)
                .commit();*/
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        if (s.equals(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    public static void getAddressFromLocation(final Location location, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> list = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        // sending back first address line and locality
                        result = address.getAddressLine(0) + ", " + address.getLocality() + ", " +  address.getCountryName() ;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Impossible to connect to Geocoder", e);
                } finally {
                    Message msg = Message.obtain();
                    msg.setTarget(handler);
                    if (result != null) {
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", result);
                        msg.setData(bundle);
                    } else
                        msg.what = 0;
                    msg.sendToTarget();
                }
            }
        };
        thread.start();
    }
    private class GeoCoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String result;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    result = bundle.getString("address");
                    break;
                default:
                    result = null;
            }
            //currentCity.setText(result);
        }
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
