package com.example.proyectobici;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.bson.Document;

import java.util.ArrayList;

// Base Stitch Packages
import com.google.android.gms.maps.model.PolylineOptions;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;

// Packages needed to interact with MongoDB and Stitch
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

// Necessary component for working with MongoDB Mobile
import com.mongodb.stitch.android.services.mongodb.local.LocalMongoDbService;




public class HistorialActivity extends AppCompatActivity {

    // Create the default Stitch Client
    final StitchAppClient client =
            Stitch.initializeDefaultAppClient("cyclistapp-kwqhc");

    // Create a Client for MongoDB Mobile (initializing MongoDB Mobile)
    final MongoClient mobileClient =
            client.getServiceClient(LocalMongoDbService.clientFactory);

    PolylineOptions po;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        MongoCollection<Document> localCollection =
                mobileClient.getDatabase("CyclistDB").getCollection("Collection_1");




        final ListView list = findViewById(R.id.historial);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("JAVA");
        arrayList.add("ANDROID");
        arrayList.add("C Language");
        arrayList.add("CPP Language");
        arrayList.add("Go Language");
        arrayList.add("AVN SYSTEMS");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, arrayList);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedItem=(String) list.getItemAtPosition(position);
                Toast.makeText(HistorialActivity.this,clickedItem,Toast.LENGTH_LONG).show();
            }
        });
    }
}
