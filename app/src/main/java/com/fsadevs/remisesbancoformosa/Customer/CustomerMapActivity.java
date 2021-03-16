package com.fsadevs.remisesbancoformosa.Customer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.fsadevs.remisesbancoformosa.DriverSelectorActivity;
import com.fsadevs.remisesbancoformosa.HistoryActivity;
import com.fsadevs.remisesbancoformosa.WelcomeActivity;
import com.fsadevs.remisesbancoformosa.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener
{

//--------------------------------------------------------------------------------------------------

    private GoogleMap mMap;
    final int LOCATION_REQUEST_CODE = 1;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private Button btnRideRequest,btnShowButtons, btnRidePlanning;
    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhone, str_RideDistance;
    private LinearLayout showButtonsLayout;
    private RelativeLayout mDriverInfoLayout;
    private LatLng pickupLatLng;
    private int radius = 1, intLinealDistance,intRoutingDistance, AutoZoom = 3;
    private Boolean driverFound = false, boolRequest = false, boolShowButtons=false, boolRouting=false;
    private String driver_ID, destination_Name, customer_ID,currentStatus,ride_ID, location_Name;
    private Marker mDriverMarker, mPickupMarker, mDestinationMarker;
    private GeoQuery geoQuery;
    private DatabaseReference driverLocationRef, driveHasEndedRef;
    private ValueEventListener driverLocationListener, driveHasEndedRefListener;
    LatLng destinationLatLng;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.routeColor};
    private HashMap dataHashmap;


//==================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        //PERMISO DE MAPAS:-------------------------------------------------------------------------
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        //------------------------------------------------------------------------------------------
        locationServiceStatusCheck();

        //INICIALIZACION DE VARIABLES:--------------------------------------------------------------
        Button btnHistory = findViewById(R.id.btn_customer_history);
        Button btnProfile = findViewById(R.id.btn_customer_settings);
        Button btnLogout = findViewById(R.id.btn_customer_logout);
        btnRidePlanning = findViewById(R.id.btn_rideplanning);
        btnRideRequest = findViewById(R.id.btn_customer_request);
        btnRideRequest.setVisibility(View.GONE);
        btnShowButtons = findViewById(R.id.btn_customer_showbuttons);
        showButtonsLayout = findViewById(R.id.customer_buttons_layout);
        showButtonsLayout.setVisibility(View.GONE);
        mDriverInfoLayout = findViewById(R.id.driver_info_layout);
        mDriverInfoLayout.setVisibility(View.GONE);
        mDriverName = findViewById(R.id.txt_driver_name);
        mDriverPhone = findViewById(R.id.txt_driver_phone);
        mDriverProfileImage = findViewById(R.id.driver_profilepic);

        polylines = new ArrayList<>();

        customer_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        str_RideDistance = findViewById(R.id.txt_ridedistance);

        //ONCLICK:----------------------------------------------------------------------------------
        btnRidePlanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent planningIntent = new Intent(CustomerMapActivity.this,DriverSelectorActivity.class);
                Bundle b = new Bundle();
                b.putDouble("destinationLAT",destinationLatLng.latitude);
                b.putDouble("destinationLNG",destinationLatLng.longitude);
                b.putDouble("locationLAT",mLastLocation.getLatitude());
                b.putDouble("locationLNG",mLastLocation.getLongitude());
                b.putString("destination_Name", destination_Name);
                b.putString("locationName", location_Name);
                planningIntent.putExtras(b);
                startActivity(planningIntent);
            }
        });

        //------------------------------------------------------------------------------------------
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
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, WelcomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
                Toast.makeText(CustomerMapActivity.this,"Sesión cerrada.",Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        });

        //------------------------------------------------------------------------------------------
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(CustomerMapActivity.this, CustomerProfileActivity.class);
                startActivity(settingsIntent);
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
            }
        });

        //------------------------------------------------------------------------------------------
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent historyIntent = new Intent(CustomerMapActivity.this, HistoryActivity.class);
                historyIntent.putExtra("userType", "Customers");
                startActivity(historyIntent);
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
            }
        });

        //------------------------------------------------------------------------------------------
        btnRideRequest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                if (boolRequest) {
                    endRide();
                } else {
                    boolRequest = true;
                    generateRideRequest();
                    getPickupLocation();
                    btnRideRequest.setText("Buscando un conductor disponible...");
                    getClosestDriver();
                }
            }
        });

        //AUTOCOMPLETE PLACES API:------------------------------------------------------------------

        Places.initialize(getApplicationContext(), "AIzaSyC2dzrACd7XCIjYYgkpTuYW_TxVNiYI95Q");
        PlacesClient placesClient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint("¿A dónde desea ir?");
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG));
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(-26.221603, -58.253926),
                new LatLng(-26.120679, -58.131311))); //Preferencia a la ciudad de formosa
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination_Name = place.getName();
                destinationLatLng = place.getLatLng();
                if (destinationLatLng != null) {
                    //DIBUJAR RUTA Y MARCADOR AL DESTINO--------------------------------------------
                    boolRouting = true;
                    if (mDestinationMarker != null) {
                        mDestinationMarker.remove();
                    }
                    mDestinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(destination_Name).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3)));
                    mDestinationMarker.showInfoWindow();
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(destinationLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    btnRideRequest.setVisibility(View.VISIBLE);
                } }
            @Override
            public void onError(Status status) { }
        });

        //------------------------------------------------------------------------------------------
        getCurrentStatus();

    }

//LOCATION SERVICE CHECKER--------------------------------------------------------------------------
    public void locationServiceStatusCheck() {
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

//STATUS MANAGER------------------------------------------------------------------------------------
    public void updateCurrentStatus(String status){
        FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customer_ID).child("Status").setValue(status);
    }

    public void getCurrentStatus() {
        DatabaseReference UserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(customer_ID).child("Status");
        UserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentStatus = dataSnapshot.getValue().toString();
                    switch (currentStatus){
                        case "free":
                            break;
                        case "engaged":
                            getDriverLocation();
                            getAssignedDriverInfo();
                            rideEndedListener();
                            break;
                        case "ongoing":
                            break;
                        case "driverwaiting":
                            break;
                        case "waiting":
                            break;
                        case "pendingtoaprove":
                            break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


    }


//ACTUALIZACION DE UBICACION------------------------------------------------------------------------
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            if (AutoZoom > 0) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                AutoZoom--;
            }
        getPickupLocation();
        //RUTA EN TIEMPO REAL-----------------------------------------------------------------------
        if (boolRouting && destinationLatLng != null) {
            getRouteToMarker(destinationLatLng);
        }
        //LLEGADA AL DESTINO------------------------------------------------------------------------
        if (intRoutingDistance != 0 && intRoutingDistance < 50) {
            boolRouting = false;
            erasePolylines();
        }
    }

//ACTUALIZAR PICKUP LOCATION MARKER
    private void getPickupLocation(){
    pickupLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
    getCurrentAddress(pickupLatLng.latitude,pickupLatLng.longitude);
    if (mPickupMarker!=null)
    { mPickupMarker.remove(); }
    if (boolRequest)
    { mPickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title(location_Name).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker4)));
      mPickupMarker.showInfoWindow();
    }
}
//--------------------------------------------------------------------------------------------------
    private void getCurrentAddress(double lat, double lng) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0);
            location_Name = address.substring(0, address.indexOf(","));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//--------------------------------------------------------------------------------------------------
    private void generateRideRequest (){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("CustomerRequests").push();
        dataHashmap = new HashMap();
        ride_ID = ref.getKey();
        dataHashmap.put("customerID",customer_ID);
        dataHashmap.put("pickupLocLATLNG", pickupLatLng);
        dataHashmap.put("pickupLocName",location_Name);
        dataHashmap.put("destinationLATLNG",destinationLatLng);
        dataHashmap.put("destination_Name", destination_Name);
        ref.updateChildren(dataHashmap);


    }

//GEOQUERY SETUP:-----------------------------------------------------------------------------------
    private void getClosestDriver() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("AvailableDrivers");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLatLng.latitude, pickupLatLng.longitude),radius);
        geoQuery.removeAllListeners();

        //GEOQUERY LISTENERS:-----------------------------------------------------------------------

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && boolRequest){
                    driverFound = true;
                    driver_ID = key;
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_ID).child("CustomerRequest");
                    HashMap map = new HashMap();
                    map.put("customerRideId",customer_ID);
                    if (destination_Name != "" && destinationLatLng!=null) {
                        map.put("destination_Name", destination_Name);
                        map.put("destinationLat", destinationLatLng.latitude);
                        map.put("destinationLng", destinationLatLng.longitude);
                    }
                    driverRef.updateChildren(map);
                    updateCurrentStatus("engaged");
                    btnRideRequest.setText("Esperando confirmación del conductor...");
                }
            }
            //--------------------------------------------------------------------------------------
            @Override
            public void onKeyExited(String key) { }
            //--------------------------------------------------------------------------------------
            @Override
            public void onKeyMoved(String key, GeoLocation location) { }
            //--------------------------------------------------------------------------------------
            @Override
            public void onGeoQueryReady() {
                if (!driverFound && radius < 20){
                    radius++;
                    getClosestDriver(); //SI NO ENCUENTRA CONDUCTOR SE LLAMA A SI MISMA
                }
            }
            //--------------------------------------------------------------------------------------
            @Override
            public void onGeoQueryError(DatabaseError error) { }
            //--------------------------------------------------------------------------------------
        });
    }

//INFORMACION DEL CONDUCTOR:------------------------------------------------------------------------
    private void getAssignedDriverInfo() {
        //ANIMACION DEL LAYOUT CUSTOMERINFO:--------------------------------------------------------
        mDriverInfoLayout.setVisibility(View.VISIBLE);
        Animation animation;
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.infolayout_in);
        mDriverInfoLayout.startAnimation(animation);
        //------------------------------------------------------------------------------------------
        FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_ID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null){
                        mDriverName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null){
                        mDriverPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("profileImageUrl") != null){
                        Picasso.get().load(map.get("profileImageUrl").toString()).into(mDriverProfileImage);
                    }
                }
            }
            //--------------------------------------------------------------------------------------
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            { }
            //--------------------------------------------------------------------------------------
        });
    }

//DRIVER LOCATION LISTENER--------------------------------------------------------------------------

    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversWorking").child(driver_ID).child("l");
        driverLocationListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            //DRIVER LOCATION LISTENER:-------------------------------------------------------------
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && boolRequest){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    btnRideRequest.setText("Conductor encontrado!");
                    btnRideRequest.setEnabled(true);
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if (mDriverMarker!=null){
                        mDriverMarker.remove();
                    }
                    //DISTANCIA LINEAL ENTRE EL CONDUCTOR Y EL CLIENTE:-----------------------------
                    Location myLocation = new Location("");
                    myLocation.setLatitude(pickupLatLng.latitude);
                    myLocation.setLongitude(pickupLatLng.longitude);
                    Location driverLocation = new Location("");
                    driverLocation.setLatitude(driverLatLng.latitude);
                    driverLocation.setLongitude(driverLatLng.longitude);
                    float distance = myLocation.distanceTo(driverLocation);
                    intLinealDistance = Math.round(distance);
                    if (intLinealDistance <50) {
                        btnRideRequest.setText("Su conductor ha llegado ");
                    }else{
                        btnRideRequest.setText("Su conductor se encuentra a: " + String.valueOf(intLinealDistance) + "m.");
                    }
                    //DRIVER MARKER:----------------------------------------------------------------
                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("El conductor está aquí.").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1)));

                }
            }
            //--------------------------------------------------------------------------------------
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
            //--------------------------------------------------------------------------------------
        });

    }

//RIDE CANCEL LISTENER------------------------------------------------------------------------------

    private void rideEndedListener() {
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_ID).child("CustomerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ }
                else{
                    endRide();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

//RESET VARIABLES Y LISTENERS-----------------------------------------------------------------------

    private void endRide() {
        boolRequest = false;
        boolRouting = false;
        geoQuery.removeAllListeners();
        erasePolylines();
        if(driverLocationListener != null) {
            driverLocationRef.removeEventListener(driverLocationListener);
        }
        if (driveHasEndedRefListener != null){
            driveHasEndedRef.removeEventListener(driveHasEndedRefListener);
        }
        if (driverFound = true) {
            FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driver_ID).child("CustomerRequest").removeValue();
            driver_ID = null;
            driverFound = false;
        }
        radius = 1;
        GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("CustomerRequests"));
        geoFire.removeLocation(customer_ID);
        if(mPickupMarker != null){
            mPickupMarker.remove();
        }
        if (mDriverMarker != null){
            mDriverMarker.remove();
        }
        if (mDestinationMarker != null){
            mDestinationMarker.remove();
        }
        updateCurrentStatus("free");

        //ANIMACION DE CERRAR EL CUSTOMERINFO LAYOUT:-----------------------------------------------
        Animation animation;
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.infolayout_out);
        mDriverInfoLayout.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                mDriverInfoLayout.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }

        });
        btnRideRequest.setText("Pedir un remis...");
        btnRideRequest.setEnabled(true);
    }












//GOOGLE MAP LISTENERS-------------------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    //API CLIENT BUILDER----------------------------------------------------------------------------
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onConnectionSuspended(int i)
    {
        Toast.makeText(CustomerMapActivity.this,"Se ha perdido la conexión con el servicio de ubicación.",Toast.LENGTH_LONG).show();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(CustomerMapActivity.this, "Por favor, habilite el servicio de ubicación",Toast.LENGTH_LONG).show();
    }

    //PERMISSION HANDLER--------------------------------------------------------------------------------
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
    protected void onStop()
    {
        super.onStop();
    }

    //DIBUJAR RUTA A LA UBICACION:----------------------------------------------------------------------
    private void getRouteToMarker(LatLng destinoLatLng) {
        Routing routing = new Routing.Builder()
                .key("AIzaSyC2dzrACd7XCIjYYgkpTuYW_TxVNiYI95Q")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),destinoLatLng)
                .build();
        routing.execute();
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
    //----------------------------------------------------------------------------------------------
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

            int colorIndex = i % COLORS.length;
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
            intRoutingDistance = route.get(i).getDistanceValue();
            str_RideDistance.setText(route.get(i).getDistanceText());
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
