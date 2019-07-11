package com.example.proyectobici;
// Base Stitch Packages
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;

// Packages needed to interact with MongoDB and Stitch
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

// Necessary component for working with MongoDB Mobile
import com.mongodb.stitch.android.services.mongodb.local.LocalMongoDbService;

import org.bson.Document;

import java.util.ArrayList;

public class guardarLocalmente {
    // Create the default Stitch Client
    final StitchAppClient client =
            Stitch.initializeDefaultAppClient("cyclistapp-kwqhc");

    // Create a Client for MongoDB Mobile (initializing MongoDB Mobile)
    final MongoClient mobileClient =
            client.getServiceClient(LocalMongoDbService.clientFactory);



    public void guardar(ArrayList<LatLng> listLocsToDraw){
        // Point to the target collection and insert a document

        MongoCollection<Document> localCollection =
                mobileClient.getDatabase("CyclistDB").getCollection("Collection_1");

        Document document = new Document();
        for(int i = 0; i < listLocsToDraw.size(); i++)
        {
            document.append("plat_"+i, listLocsToDraw.get(i).latitude);
            document.append("plon_"+i, listLocsToDraw.get(i).longitude);
        }

        localCollection.insertOne(document);

// Find the first document
 //       Document doc = localCollection.find().first();

//Find all documents that match the find criteria
//        Document query = new Document();
//        query.put("name", new BsonString("veirs"));

 //       FindIterable<Document> cursor = localCollection.find(query);
//       ArrayList<Document> results =
 //               (ArrayList<Document>) cursor.into(new ArrayList<Document>());
    }

    public PolylineOptions verFirst(){

        MongoCollection<Document> localCollection =
                mobileClient.getDatabase("CyclistDB").getCollection("Collection_1");

        Document doc = localCollection.find().first();
        PolylineOptions po = new PolylineOptions();
        for(int i = 0; i < doc.size(); i++)
        {

            Double lat = (Double) doc.get("plat_"+i);
            Double lon = (Double) doc.get("plon_"+i);
            System.out.println(lat+" "+lon);
            if(lat!= null && lon != null)
                po.add(new LatLng(lat,lon));
        }
            //po.add(doc.get("plat_"+i));

        return po;
    }
}
