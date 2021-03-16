package com.fsadevs.remisesbancoformosa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.fsadevs.remisesbancoformosa.Customer.CustomerLoginActivity;
import com.fsadevs.remisesbancoformosa.Driver.DriverLoginActivity;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnDriver, bntCustomer;
    private ImageView imgLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        bntCustomer = findViewById(R.id.btn_Customer);
        btnDriver = findViewById(R.id.btn_Driver);
        imgLogo = findViewById(R.id.img_main_logo);

        new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                imgLogo.setVisibility(View.VISIBLE);
                Animation animation;
                animation = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.fall);
                imgLogo.startAnimation(animation);
            }
        }.start();

        bntCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent customerIntent = new Intent(WelcomeActivity.this, CustomerLoginActivity.class);
                startActivity(customerIntent);
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                finish();
            }
        });

        btnDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent driverIntent = new Intent(WelcomeActivity.this, DriverLoginActivity.class);
                startActivity(driverIntent);
                overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                finish();
            }
        });


    }

}
