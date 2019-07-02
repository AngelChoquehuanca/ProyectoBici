package com.example.proyectobici;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DispositivosBT extends AppCompatActivity {

    private static final String TAG="DispositivosBT";
    private ListView lstDispositivos;
    public static String EXTRA_DEVICE_ADDRESS="device_address";

    private BluetoothAdapter adaptadorBT;
    private ArrayAdapter<String> arregloDispositivos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos_bt);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificarEstadoBT();

        arregloDispositivos=new ArrayAdapter<String>(this,R.layout.nombre_dispositivos);
        lstDispositivos=(ListView)findViewById(R.id.cmpListBT);
        lstDispositivos.setAdapter(arregloDispositivos);
        lstDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info=((TextView) view).getText().toString();
                String address=info.substring(info.length()-17);

                Intent it=new Intent(DispositivosBT.this, RouteActivity.class);
                it.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startActivity(it);
            }
        });

        adaptadorBT=BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> dispositivosEncontrados=adaptadorBT.getBondedDevices();
        if(dispositivosEncontrados.size()>0){
            for (BluetoothDevice device:dispositivosEncontrados){
                arregloDispositivos.add(device.getName()+"\n"+device.getAddress());
            }
        }
    }

    private void VerificarEstadoBT(){
        adaptadorBT=BluetoothAdapter.getDefaultAdapter();
        if (adaptadorBT==null){
            Toast.makeText(getBaseContext(),"El dispositivo no soporta Bluetooth",Toast.LENGTH_SHORT).show();
        }else {
            if (adaptadorBT.isEnabled()){
                Log.d(TAG,"...Bluetooth Activado...");
            }else {
                Intent habilitarBTIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(habilitarBTIntent, 1);
            }
        }
    }
}
