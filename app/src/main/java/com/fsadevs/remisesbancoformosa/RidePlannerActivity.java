package com.fsadevs.remisesbancoformosa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RidePlannerActivity extends AppCompatActivity {

    private String driverID, customerID,destinationName, locationName;
    private DatabaseReference mDatabase;
    private EditText bxtimeSelector, bxdestination, bxstartloc;
    private TextView driverName, driverProv;
    private CircleImageView driverPic;
    public  final Calendar c = Calendar.getInstance();
    final int hora = c.get(Calendar.HOUR_OF_DAY);
    final int minuto = c.get(Calendar.MINUTE);
    private Double destinationLAT,destinationLNG,locationLAT,locationLNG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_planner);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driverID = getIntent().getExtras().getString("driverID");
        bxtimeSelector = findViewById(R.id.txtbx_timeselect);
        bxstartloc = findViewById(R.id.txtbx_startloc);
        bxdestination = findViewById(R.id.txtbx_destination);
        driverName = findViewById(R.id.txt_rideplanner_driverName);
        driverProv = findViewById(R.id.txt_rideplanner_driverProv);
        driverPic = findViewById(R.id.rideplanner_driver_profileimage);
        destinationLAT = getIntent().getExtras().getDouble("destinationLAT");
        destinationLNG = getIntent().getExtras().getDouble("destinationLNG");
        locationLAT = getIntent().getExtras().getDouble("locationLAT");
        locationLNG = getIntent().getExtras().getDouble("locationLNG");
        destinationName = getIntent().getExtras().getString("destinationName");
        bxdestination.setText(destinationName);
        locationName = getIntent().getExtras().getString("locationName");
        bxstartloc.setText(locationName);




        Button btn_TimePick = findViewById(R.id.btn_timepicker);


        btn_TimePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePicker();
            }
        });

        getDriverData();

    }
//--------------------------------------------------------------------------------------------------
    private void timePicker() {
        TimePickerDialog mtimer = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String horaFormateada =  (hourOfDay < 10)? String.valueOf("0" + hourOfDay) : String.valueOf(hourOfDay);
                String minutoFormateado = (minute < 10)? String.valueOf("0" + minute):String.valueOf(minute);
                bxtimeSelector.setText(horaFormateada + ":" + minutoFormateado);
            }
        }, hora, minuto, true);
        mtimer.show();
    }
//--------------------------------------------------------------------------------------------------
    private void getDriverData(){
        mDatabase.child("Users").child("Drivers").child(driverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null){
                        driverName.setText(map.get("name").toString());
                    }
                    if (map.get("proveedor") != null){
                        driverProv.setText(map.get("proveedor").toString());
                    }
                    if (map.get("profileImageUrl") != null){
                        Picasso.get().load(map.get("profileImageUrl").toString()).into(driverPic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }






}

