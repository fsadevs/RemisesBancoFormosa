package com.fsadevs.remisesbancoformosa.Driver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.fsadevs.remisesbancoformosa.HistoryActivity;
import com.fsadevs.remisesbancoformosa.WelcomeActivity;
import com.fsadevs.remisesbancoformosa.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener
{

    private GoogleMap mMap;
    final int LOCATION_REQUEST_CODE = 1;
    GoogleApiClient mGoogleApiClient;
    Location driverLastLocation;
    LocationRequest mLocationRequest;
    private Button btnRideStatus, btnRideCancel, btnShowButtons;
    private String customer_ID = "", location_ID, driver_ID, request_ID,driverName, driverProv, strDistancia, ride_ID, pickup_Name, destination_Name;
    private Boolean boolLoggingOut = false, boolRouting = false, boolShowButtons=false;
    private Marker mPickupMarker, mDestinationMarker;
    private LatLng destination_LatLng, pickup_LatLng, routing_LatLng;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;
    private RelativeLayout customerInfoLayout, showButtonsLayout;
    private CircleImageView imgCustomerProfileImage;
    private TextView txt_CustomerName, txt_CustomerLocation, txt_Distancia;
    private int  status = 0, intDistancia;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.routeColor};



//--------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else{
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        LocationServiceStatusCheck();

        //VARIABLES:--------------------------------------------------------------------------------
        destination_LatLng = new LatLng(0.0, 0.0);
        polylines = new ArrayList<>();
        customerInfoLayout = findViewById(R.id.customer_info_layout);
        customerInfoLayout.setVisibility(View.GONE);
        showButtonsLayout = findViewById(R.id.driver_anim_buttons_layout);
        showButtonsLayout.setVisibility(View.GONE);
        txt_CustomerName = findViewById(R.id.txt_customer_name);
        txt_CustomerLocation = findViewById(R.id.txt_customer_location);
        imgCustomerProfileImage = findViewById(R.id.customer_profilepic);
        btnShowButtons = findViewById(R.id.btn_driver_show_buttons);
        btnRideStatus = findViewById(R.id.btn_rideStatus);
        btnRideCancel = findViewById(R.id.btn_rideCancel);
        Button btnSettings = findViewById(R.id.btn_driver_settings);
        Button btnLogout = findViewById(R.id.btn_driver_logout);
        Button btnHistory = findViewById(R.id.btn_driver_history);
        txt_Distancia = findViewById(R.id.txt_kilometraje);
        driver_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //DriverInfo--------------------------------------------------------------------------------
        DatabaseReference driverInfo = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_ID);
        driverInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        driverName = String.valueOf(map.get("name")); }
                    if (map.get("proveedor") != null) {
                        driverProv = String.valueOf(map.get("proveedor")); }
                }
            }@Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


        //ONCLICK LISTENERS:------------------------------------------------------------------------
        btnShowButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!boolShowButtons) {
                    boolShowButtons = true;
                    btnShowButtons.setBackgroundResource(R.drawable.animbutton_hidelayout);
                    showButtonsLayout.setVisibility(View.VISIBLE);
                    Animation animation;
                    animation = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.zoomin);
                    showButtonsLayout.startAnimation(animation);
                }else{
                    boolShowButtons = false;
                    btnShowButtons.setBackgroundResource(R.drawable.animbutton_showlayout);
                    Animation animation;
                    animation = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.zoomout);
                    showButtonsLayout.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            showButtonsLayout.setVisibility(View.GONE);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });
                }
            }
        });

        //------------------------------------------------------------------------------------------
        btnRideCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endRide();
            }
        });

        //------------------------------------------------------------------------------------------
        btnRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (status){
                    case 1://Aceptar cliente
                        btnRideStatus.setBackgroundResource(R.drawable.ic_placeholder);
                        recordRide();
                        break;
                    case 2: // Llegada a localizacion del cliente

                        erasePolylines();
                        if (destination_LatLng.latitude != 0.0 && destination_LatLng.longitude != 0.0){
                            //DIBUJAR RUTA AL DESTINO DEL CLIENTE:----------------------------------
                            routing_LatLng =(destination_LatLng);
                            boolRouting = true;
                            mDestinationMarker = mMap.addMarker(new MarkerOptions().position(destination_LatLng).title("Destino del viaje.").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1)));
                        }
                        btnRideStatus.setBackgroundResource(R.drawable.animbutton_cancel);
                        recordRide();
                        break;

                    case 3: //Trabajo terminado
                        recordRide();
                        endRide();
                        break;
                }
            }
        });
        //------------------------------------------------------------------------------------------
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent driverIntent = new Intent(DriverMapActivity.this, DriverProfileActivity.class);
                startActivity(driverIntent);
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
            }
        });
        //------------------------------------------------------------------------------------------
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent historyIntent = new Intent(DriverMapActivity.this, HistoryActivity.class);
                historyIntent.putExtra("userType", "Drivers");
                startActivity(historyIntent);
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
            }
        });

        //------------------------------------------------------------------------------------------
        btnLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                boolLoggingOut = true;
                stopService(new Intent(DriverMapActivity.this, RideRequestListenerService.class));
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent mainIntent = new Intent(DriverMapActivity.this, WelcomeActivity.class);
                startActivity(mainIntent);
                Toast.makeText(DriverMapActivity.this,"Sesión cerrada.",Toast.LENGTH_SHORT).show();
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
                finish();
            }
        });
        //------------------------------------------------------------------------------------------
        getAssignedCustomer();

    }

//LOCATION SERVICE CHECKER--------------------------------------------------------------------------
    public void LocationServiceStatusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El servicio de ubicación esta desactivado. ¿Desea activarlo ahora?")
                .setCancelable(false)
                .setPositiveButton(" [ SÍ ] ", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(" [ NO ] ", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

//CUSTOMER REQUEST LISTENER:------------------------------------------------------------------------
    private void getAssignedCustomer() {
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_ID).child("CustomerRequest");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    status = 1;
                        for (DataSnapshot rideData: dataSnapshot.getChildren()) {
                            ride_ID = rideData.getKey();
                            getRideData(ride_ID);
                        }
                        getAssignedCustomerPickupLocation();
                        getAssignedCustomerInfo();
                        getAssignedCustomerDestination();
                }
                else{
                    endRide();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
//--------------------------------------------------------------------------------------------------
    private void getRideData(String ride_id) {
        FirebaseDatabase.getInstance().getReference().child("CustomerRequests").child(ride_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                            Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            if (map.get("customerID")!=null){
                                customer_ID = map.get("customerID").toString();
                            }
                            if (map.get("pickupLocLATLNG")!=null){
                                pickup_LatLng = getLATLNG("pickupLocLATLNG");
                            }
                            if (map.get("pickupLocName")!=null){
                                pickup_Name = map.get("pickupLocName").toString();
                            }
                            if (map.get("destinationLATLNG")!=null){
                                destination_LatLng = getLATLNG("destinationLATLNG");
                            }
                            if (map.get("destinationName")!=null){
                                destination_Name = map.get("destinationName").toString();
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private LatLng getLATLNG(String DatabaseREF){
        final LatLng[] result = new LatLng[1];
        FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(ride_ID).child(DatabaseREF)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            double latitude = 0.0, longitude=0.0;
                            Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            if (map.get("latitude")!=null){
                                latitude = Double.parseDouble(map.get("latitude").toString());
                            }
                            if (map.get("longitude")!=null){
                                longitude = Double.parseDouble(map.get("longitude").toString());
                            }
                            result[0] = new LatLng(latitude,longitude);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
        return result[0];
    }

    //UBICACION DEL CLIENTE:----------------------------------------------------------------------------
    private void getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequests").child(customer_ID).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customer_ID.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0)!=null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1)!=null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    //COLOCAR MARCADOR EN UBICACION:------------------------------------------------
                    pickup_LatLng = new LatLng(locationLat, locationLng);
                    mPickupMarker = mMap.addMarker(new MarkerOptions().position(pickup_LatLng).title("El cliente está aquí.").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2)));
                    //DIBUJAR RUTA A LA UBICACION DEL CLIENTE:--------------------------------------
                    boolRouting = true;
                    routing_LatLng = pickup_LatLng;
                    //MOVER CAMARA:-----------------------------------------------------------------
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pickup_LatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

//INFORMACION DEL CLIENTE:--------------------------------------------------------------------------
    private void getAssignedCustomerInfo() {
        //ANIMACION DEL LAYOUT CUSTOMERINFO:--------------------------------------------------------
        customerInfoLayout.setVisibility(View.VISIBLE);
        Animation animation;
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.infolayout_in);
        customerInfoLayout.startAnimation(animation);

        //CUSTOMER INFO LISTENER:-------------------------------------------------------------------
        FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customer_ID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null){
                        txt_CustomerName.setText(map.get("name").toString());
                    }
                    if (map.get("profileImageUrl") != null){
                        Picasso.get().load(map.get("profileImageUrl").toString()).into(imgCustomerProfileImage);
                    }
                    txt_Distancia.setText("Distancia: Esperando datos...");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

//DESTINO DEL CLIENTE:------------------------------------------------------------------------------
    private void getAssignedCustomerDestination() {
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_ID).child("CustomerRequest");
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    Double destinationLat = 0.0, destinationLng = 0.0;
                    if (map.get("destinationLat")!=null){
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if (map.get("destinationLng")!=null){
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destination_LatLng = new LatLng(destinationLat,destinationLng);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            { }
        });
    }

//ROUTING-------------------------------------------------------------------------------------------
    private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .key("AIzaSyC2dzrACd7XCIjYYgkpTuYW_TxVNiYI95Q")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(driverLastLocation.getLatitude(), driverLastLocation.getLongitude()),routing_LatLng)
                .build();
        routing.execute();
    }

//RESET DE VARIBALES Y LISTENERS:-------------------------------------------------------------------
    private void endRide() {
        request_ID = "";
        btnRideStatus.setBackgroundResource(R.drawable.ic_accept);
        btnRideCancel.setVisibility(View.VISIBLE);
        btnRideCancel.setEnabled(true);
        erasePolylines();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_ID).child("CustomerRequest");
        driverRef.removeValue();
        status = 0;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequests");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customer_ID);
        customer_ID = "";
        if (mDestinationMarker != null) {
            mDestinationMarker.remove();
        }
        if (mPickupMarker != null){
            mPickupMarker.remove();
        }
        erasePolylines();
        boolRouting = false;
        customer_ID = "";
        if (assignedCustomerPickupLocationRef != null) {
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        //ANIMACION DE CERRAR EL CUSTOMERINFO LAYOUT:-----------------------------------------------
        Animation animation;
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.infolayout_out);
        customerInfoLayout.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                customerInfoLayout.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }

        });
        //------------------------------------------------------------------------------------------
        txt_CustomerName.setText("");
        imgCustomerProfileImage.setImageResource(R.drawable.default_profile_pic);
    }

//GUARDAR VIAJE-------------------------------------------------------------------------------------
    public void recordRide() {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_ID).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customer_ID).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("History");
        if (status==1) {
            request_ID = historyRef.push().getKey();
            driverRef.child(request_ID).setValue(true);
            customerRef.child(request_ID).setValue(true);
        }
        HashMap map = new HashMap();
        if (status==3){
            map.put("endtime", getCurrentTimestamp());
        }
        if (status==2) {
            map.put("destination", location_ID);
            map.put("distance", intDistancia);
            map.put("starttime", getCurrentTimestamp());
            status = 3;
        }
        if (status == 1) {
            map.put("driver", driverName);
            map.put("customer", txt_CustomerName.getText());
            map.put("proveedor", driverProv);
            map.put("startlocation", location_ID);
            status = 2;
        }
        historyRef.child(request_ID).updateChildren(map);

    }
    //----------------------------------------------------------------------------------------------
    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }

    //GOOGLE MAPS API:------------------------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }


    //ACTUALIZACION DE LOCALIZACION ACTUAL----------------------------------------------------------
    @Override
    public void onLocationChanged(Location location)
    {
        if (getApplicationContext() != null) {
            driverLastLocation = location;
            LatLng driverLastLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLastLocationLatLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            //SWITCH DRIVER AVAILABLE/WORKING-------------------------------------------------------
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("AvailableDrivers");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriversWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);
            if ("".equals(customer_ID)) {
                geoFireWorking.removeLocation(driver_ID);
                geoFireAvailable.setLocation(driver_ID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                status = 0;
            } else {
                geoFireAvailable.removeLocation(driver_ID);
                geoFireWorking.setLocation(driver_ID, new GeoLocation(location.getLatitude(), location.getLongitude()));
            }
            //MARCAR RUTAS EN TIEMPO REAL
            if (boolRouting && routing_LatLng!=null){
                getRouteToMarker();
            }

            //MOSTRAR INFORMACION DEL DESTINO DE UNA RUTA DIBUJADA----------------------------------
            if(intDistancia != 0 && status>0) {
                txt_Distancia.setText("Distancia: " + strDistancia);
                txt_CustomerLocation.setText(location_ID);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onConnectionSuspended(int i)
    {
        Toast.makeText(DriverMapActivity.this,"Se ha perdido la conexión.",Toast.LENGTH_SHORT).show();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

//DESCONECTAR AL CONDUCTOR:-------------------------------------------------------------------------
    private void disconnectDriver(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("AvailableDrivers");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(driver_ID);
    }

//PERMISSION HANDLER:-------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            } else {
                Toast.makeText(getApplicationContext(), "Por favor, active los permisos de localización.", Toast.LENGTH_SHORT).show();
            }
        }
    }

//--------------------------------------------------------------------------------------------------
    @Override
    protected void onStop() {
        super.onStop();
        if (boolLoggingOut) {
            disconnectDriver();
        }
    }
//--------------------------------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();

    }

//ROUTING LISTENERS---------------------------------------------------------------------------------
    @Override
    public void onRoutingStart()
    { }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null){
            Toast.makeText(this,"Error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"Ha ocurrido un error al trazar la ruta.",Toast.LENGTH_SHORT).show();
        }
    }
    //DIBUJAR POLILINEA-----------------------------------------------------------------------------
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }
        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
            intDistancia = route.get(i).getDistanceValue();
            strDistancia = route.get(i).getDistanceText();
            location_ID = route.get(i).getEndAddressText();
        }
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onRoutingCancelled()
    { }
//BORRAR RUTA DIBUJADA:-----------------------------------------------------------------------------
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
//=========================================END======================================================
}
