package com.fsadevs.remisesbancoformosa;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fsadevs.remisesbancoformosa.Customer.CustomerMapActivity;
import com.fsadevs.remisesbancoformosa.Driver.DriverMapActivity;
import com.fsadevs.remisesbancoformosa.Driver.RideRequestListenerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private AnimationDrawable animationDrawable;
    private FrameLayout layout;
    private ImageView imgVolante;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        layout = findViewById(R.id.splash_layout);
        imgVolante = findViewById(R.id.volante);
        //BACKGROUND GRADIENT ANIMATION:------------------------------------------------------------
        animationDrawable =(AnimationDrawable)layout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(1500);
        animationDrawable.start();
        //VOLANTE ANIMATION:------------------------------------------------------------------------
        Animation animation;
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.rotate);
        imgVolante.startAnimation(animation);


        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    new CountDownTimer(3000, 1000) {

                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            customerType();
                        }
                    }.start();
                } else {
                    new CountDownTimer(5000, 1000) {

                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            Intent mainIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
                            startActivity(mainIntent);
                            overridePendingTransition(R.anim.left_in, R.anim.left_out);
                            finish();
                        }
                    }.start();
                }
            }
        };


    }

    public void customerType() {
        final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference customerTypeRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        customerTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id)) {
                    Intent customerIntent = new Intent(SplashActivity.this, CustomerMapActivity.class);
                    startActivity(customerIntent);
                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                    Toast.makeText(SplashActivity.this, "Sesión iniciada con éxito.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    DriverType();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void DriverType() {
        final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference driverTypeRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
        driverTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id)) {

                    Intent driverIntent = new Intent(SplashActivity.this, DriverMapActivity.class);

                    startService(new Intent(SplashActivity.this, RideRequestListenerService.class));
                    startActivity(driverIntent);
                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                    Toast.makeText(SplashActivity.this, "Sesión de conductor iniciada con éxito.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Intent mainIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
                    startActivity(mainIntent);
                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                    finish();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}