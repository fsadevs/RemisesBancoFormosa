package com.fsadevs.remisesbancoformosa;


import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fsadevs.remisesbancoformosa.historyRecyclerView.HistoryAdapter;
import com.fsadevs.remisesbancoformosa.historyRecyclerView.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class HistoryActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String userType, userID;
    private HistoryAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<HistoryObject> mObjectsList = new ArrayList<>();
    private TextView txtvw_title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        txtvw_title = findViewById(R.id.txt_history_title);
        mRecyclerView = findViewById(R.id.historyRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button btnBack = findViewById(R.id.btn_history_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
            }
        });

        userType = getIntent().getExtras().getString("userType");

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(userType!= null && userID!=null) {
            getDataFromFirebase();
            if (userType.equals("Drivers")){
                txtvw_title.setText("Historial del conductor");
            }else if (userType.equals("Customers")){
                txtvw_title.setText("Historial del usuario");
            }
        }
    }

    private void getDataFromFirebase(){
        mDatabase.child("Users").child(userType).child(userID).child("history")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                            mObjectsList.clear();
                            for(DataSnapshot history : dataSnapshot.getChildren()) {
                                FetchRideInformation(history.getKey());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

    }

    private void FetchRideInformation(String rideKey) {
        mDatabase.child("History").child(rideKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String Name="Nombre: ---", StartLocation = "Origen: ---", Destination = "Destino: ---",
                            Distance = "Distancia: ---";
                    Long TimeStp = 0L;
                    for (DataSnapshot items : dataSnapshot.getChildren()) {

                        if (userType.equals("Customers")){
                        if (items.getKey().equals("driver") && items.getValue() != null) {
                            Name = ("Conductor: "+(items.getValue()).toString());
                        }}
                        if (userType.equals("Drivers")){
                        if (items.getKey().equals("customer") && items.getValue() != null) {
                            Name = ("Pasajero: "+(items.getValue()).toString());
                        }}
                        if (items.getKey().equals("startlocation") && items.getValue() != null) {
                            StartLocation = ("Origen: "+(items.getValue()).toString());
                        }
                        if (items.getKey().equals("destination") && items.getValue() != null) {
                            Destination = ("Destino: "+(items.getValue()).toString());
                        }
                        if (items.getKey().equals("distance") && items.getValue() != null) {
                            Distance = ("Distancia: "+(items.getValue()).toString()+" m.");
                        }
                        if (items.getKey().equals("starttime") && items.getValue() != null) {
                            TimeStp = Long.valueOf(items.getValue().toString());
                        }

                    }
                    mObjectsList.add(new HistoryObject(getDate(TimeStp), Name, StartLocation, Destination, Distance));
                    mAdapter = new HistoryAdapter(mObjectsList, R.layout.item_history);
                    mRecyclerView.setAdapter(mAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();
        return date;
    }

}