package com.fsadevs.remisesbancoformosa.Driver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fsadevs.remisesbancoformosa.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;
    private Button mLogin;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private TextView mNoAccount, mStatusLabel;
    private Boolean LoginBool = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAuth = FirebaseAuth.getInstance();

        mEmail =  findViewById(R.id.tbx_driver_email);
        mPassword =  findViewById(R.id.tbx_driver_password);
        progressDialog = new ProgressDialog(this);
        mLogin =  findViewById(R.id.btn_driver_login);
        mNoAccount = findViewById(R.id.txt_driver_register);
        mStatusLabel = findViewById(R.id.txt_driver_label);

        mNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginBool = false;
                mNoAccount.setVisibility(View.INVISIBLE);
                mLogin.setText("Registrarse");
                mStatusLabel.setText("Registre su cuenta de conductor");

            }
        });



        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LoginBool){
                    LoginUser();
                }
                else{
                    RegisterUser();
                }
            }
        });
    }

    public void LoginUser(){
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(DriverLoginActivity.this, "Por favor, ingrese su email.", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(DriverLoginActivity.this, "Por favor, ingrese su contraseña.", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Iniciando sesión:");
            progressDialog.setMessage("Por favor, espere mientras comprobamos sus datos ...");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(DriverLoginActivity.this, "Ha ocurrido un error al iniciar sesión. Por favor, intente nuevamente.", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(DriverLoginActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(DriverLoginActivity.this, DriverMapActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });

        }
    }

    public void RegisterUser(){
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(DriverLoginActivity.this, "Por favor, ingrese su email.", Toast.LENGTH_SHORT).show();
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(DriverLoginActivity.this, "Por favor, ingrese su contraseña.", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.setTitle("Registrando conductor:");
                    progressDialog.setMessage("Por favor espere, estamos registrando sus datos...");
                    progressDialog.show();
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(DriverLoginActivity.this, "No se ha podido registrar el usuario. Por favor, intente nuevamente.", Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.dismiss();
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id).child("email");
                                current_user_db.setValue(email);
                                Toast.makeText(DriverLoginActivity.this, "Registro de conductor exitoso.", Toast.LENGTH_SHORT).show();
                                Intent regintent = new Intent(DriverLoginActivity.this, DriverProfileActivity.class);
                                startActivity(regintent);
                                finish();

                            }
                        }
                    });
                }

    }

}
