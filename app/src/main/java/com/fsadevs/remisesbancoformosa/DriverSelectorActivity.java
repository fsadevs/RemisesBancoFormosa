package com.fsadevs.remisesbancoformosa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.fsadevs.remisesbancoformosa.driversRecyclerView.DriverObject;
import com.fsadevs.remisesbancoformosa.driversRecyclerView.DriversAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DriverSelectorActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private DriversAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<DriverObject> mObjectsList = new ArrayList<>();
    private Double destinationLAT, destinationLNG, locationLAT, locationLNG;
    private String destinationName, locationName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_selector);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRecyclerView = findViewById(R.id.rideplaningRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getDataFromFirebase();
        Button btnBack = findViewById(R.id.btn_driverselector_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
            }
        });
        destinationLAT = getIntent().getExtras().getDouble("destinationLAT");
        destinationLNG = getIntent().getExtras().getDouble("destinationLNG");
        destinationName = getIntent().getExtras().getString("destinationName");
        locationLAT = getIntent().getExtras().getDouble("locationLAT");
        locationLNG = getIntent().getExtras().getDouble("locationLNG");
        locationName = getIntent().getExtras().getString("locationName");

    }

    private void getDataFromFirebase(){
        mDatabase.child("AvailableDrivers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                            mObjectsList.clear();
                            for(DataSnapshot items : dataSnapshot.getChildren()) {
                                FetchRideInformation(items.getKey());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

    }

    private void FetchRideInformation(String key) {
        mDatabase.child("Users").child("Drivers").child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String driverName = "Nombre", driverProv = "Proveedor", driverPicURL ="",driverID="";
                            driverID = dataSnapshot.getKey();
                            for (DataSnapshot items : dataSnapshot.getChildren()) {

                                if (items.getKey().equals("name") && items.getValue() != null) {
                                    driverName = (items.getValue()).toString();
                                }
                                if (items.getKey().equals("proveedor") && items.getValue() != null) {
                                    driverProv = (items.getValue()).toString();
                                }
                                if (items.getKey().equals("profileImageUrl") && items.getValue() != null) {
                                    driverPicURL = (items.getValue()).toString();
                                }

                            }
                            mObjectsList.add(new DriverObject(driverName, driverProv, driverPicURL,driverID));
                            mAdapter = new DriversAdapter(mObjectsList, R.layout.item_drivers);
                            mAdapter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(DriverSelectorActivity.this, RidePlannerActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString("driverID", mObjectsList.get(mRecyclerView.getChildAdapterPosition(view)).getDriverID());
                                    b.putDouble("destinationLAT",destinationLAT);
                                    b.putDouble("destinationLNG",destinationLNG);
                                    b.putDouble("locationLAT",locationLAT);
                                    b.putDouble("locationLNG",locationLNG);
                                    b.putString("destinationName",destinationName);
                                    b.putString("locationName",locationName);
                                    intent.putExtras(b);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                }
                            });
                            mRecyclerView.setAdapter(mAdapter);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }


}
