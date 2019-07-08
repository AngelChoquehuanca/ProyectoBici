package com.example.proyectobici;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SiginActivity extends AppCompatActivity {
    //private TextView mTextMessage;
    private NetworkFragment mNetworkFragment;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtNombre;
    private EditText edtDireccion;
    private EditText edtNacimiento;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sigin_cancel:
                    //mTextMessage.setText(R.string.title_cancel);
                    Intent iCancel=new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(iCancel);
                    return true;
                case R.id.signin_check:
                    Thread tr=new Thread(){
                        @Override
                        public void run() {
                            String email=edtEmail.getText().toString();
                            String pass=edtPassword.getText().toString();
                            final String resultado=enviarDatosPost(edtEmail.getText().toString(),
                                    edtPassword.getText().toString(),edtNombre.getText().toString(),
                                    edtDireccion.getText().toString(),edtNacimiento.getText().toString());
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    boolean res=obtenerJSON(resultado);
                                    if(res){
                                        Intent i=new Intent(getApplicationContext(),RouteActivity.class);
                                        i.putExtra("cod",4);
                                        startActivity(i);
                                    }else {
                                        Toast.makeText(getApplicationContext(),"Usuario o Password Incorrecto",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });*/
                        }
                    };
                    tr.start();

                    Intent iCheck=new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(iCheck);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sigin);
        edtEmail=(EditText)findViewById(R.id.cmpSiginEmail);
        edtPassword=(EditText)findViewById(R.id.cmpSiginPassword);
        edtNombre=(EditText)findViewById(R.id.cmpSiginNombre);
        edtDireccion=(EditText)findViewById(R.id.cmpSiginDireccion);
        edtNacimiento=(EditText)findViewById(R.id.cmpSiginFecha);



        BottomNavigationView navView = findViewById(R.id.nav_sigin);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public String enviarDatosPost(String email, String pass, String nombre, String direccion, String nacimiento){
        StringBuilder resul=null;
        int respuesta=0;
        String linea="";
        Log.i("SiginActivity","Email:"+email+",Pass:"+pass+",Nombre:"+nombre+",Direccion:"+direccion+",Nacimiento:"+nacimiento);
        String urlParameters="log_email="+email+"&log_pass="+pass+"&usu_nombre="+nombre+"&usu_direccion="+direccion+"&usu_nacimiento="+nacimiento;
        try {
            URL url=new URL("https://afternoon-mesa-67144.herokuapp.com/rest/usuario.php");
            HttpURLConnection conexion=(HttpURLConnection)url.openConnection();
            conexion.setRequestMethod("POST");
            conexion.setRequestProperty("Content-Length",Integer.toString(urlParameters.getBytes().length));
            conexion.setDoOutput(true);
            DataOutputStream wr=new DataOutputStream(conexion.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();
            InputStream is=conexion.getInputStream();
            respuesta=conexion.getResponseCode();

            resul=new StringBuilder();
            if(respuesta==HttpURLConnection.HTTP_OK){
                InputStream in=new BufferedInputStream(conexion.getInputStream());
                BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                while((linea=reader.readLine())!=null){
                    resul.append(linea);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("SiginActivity",resul.toString());
        return resul.toString();
    }

    public boolean obtenerJSON(String response){
        boolean res=false;
        try {
            JSONArray json=new JSONArray(response);
            //Toast.makeText(this, json.toString(), Toast.LENGTH_SHORT).show();
            if (json.length()>0){
                res=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return res;
    }
}
