package com.example.proyectobici;

import android.os.AsyncTask;
import android.util.Log;

import com.example.proyectobici.NearbyPlaces.DataParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;



public class sendRoute extends AsyncTask<Object, String, String> {
    String server_response;

    @Override
    protected String doInBackground(Object... objects) {
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL("https://afternoon-mesa-67144.herokuapp.com/rest/gps.php");

            urlConnection = (HttpURLConnection) url
                    .openConnection();
            urlConnection.setDoInput (true);
            urlConnection.setDoOutput (true);
            urlConnection.setUseCaches (false);
            urlConnection.setRequestProperty("Content-Type","application/json");
            /*int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                server_response = readStream(urlConnection.getInputStream());
                Log.v("CatalogClient", server_response);
            }*/
            DataOutputStream printout;

            printout = new DataOutputStream(urlConnection.getOutputStream ());
            printout.writeBytes(URLEncoder.encode(createObject().toString(),"UTF-8"));
            printout.flush ();
            printout.close ();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        System.out.println(s);

    }


    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();


    }
    public JSONObject createObject(){
        JSONObject rutagps = new JSONObject();
        try {
            rutagps.put("gps_latitud", "11.1234567");
            rutagps.put("gps_longitud", "121.7654321");
            rutagps.put("gps_fecha", "2015-01-011");
            rutagps.put("gps_tiempo", "14:00:00");
            rutagps.put("fk_id_ruta", "1");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return rutagps;
    }
}