package com.example.proyectobici;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLoguear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtEmail=(EditText)findViewById(R.id.cmpEdtEmail);
        edtPassword=(EditText)findViewById(R.id.cmpEdtPassword);
        btnLoguear=(Button)findViewById(R.id.cmpBtnLoguear);

        btnLoguear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread tr=new Thread(){
                    @Override
                    public void run() {
                        String email=edtEmail.getText().toString();
                        String pass=edtPassword.getText().toString();
                        final String resultado=enviarDatos(email,pass);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                boolean res=obtenerJSON(resultado);
                                if(res){
                                    Intent i=new Intent(getApplicationContext(),MainActivity.class);
                                    i.putExtra("cod",4);
                                    startActivity(i);
                                }else {
                                    Toast.makeText(getApplicationContext(),"Usuario o Password Incorrecto",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                };
                tr.start();
            }
        });
    }

    public String enviarDatos(String email, String pass){
        URL url=null;
        String linea="";
        int respuesta=0;
        StringBuilder resul=null;

        try {
            url=new URL("https://afternoon-mesa-67144.herokuapp.com/rest/login.php?log_email="+email+"&log_pass="+pass);
            HttpURLConnection conexion=(HttpURLConnection)url.openConnection();
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

        return resul.toString();
    }

    public boolean obtenerJSON(String response){
        boolean res=false;
        try {
            JSONArray json=new JSONArray(response);
            if (json.length()>0){
                res=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return res;
    }
}
