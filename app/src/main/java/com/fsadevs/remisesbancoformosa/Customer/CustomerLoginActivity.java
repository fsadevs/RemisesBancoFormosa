package com.fsadevs.remisesbancoformosa.Customer;

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

public class CustomerLoginActivity extends AppCompatActivity {
    private EditText mEmail, mPassword;
    private Button mLogin;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private TextView mNoAccount, mStatusLabel;
    private Boolean registerBool = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.tbx_customer_email);
        mPassword = findViewById(R.id.tbx_customer_password);
        mNoAccount = findViewById(R.id.txt_customer_register);
        mStatusLabel = findViewById(R.id.txt_customer_label);
        mLogin = findViewById(R.id.btn_customer_login);
        progressDialog = new ProgressDialog(this);

        mNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerBool = false;
                mNoAccount.setVisibility(View.INVISIBLE);
                mStatusLabel.setText("Registre su cuenta de usuario");
                mLogin.setText("Registrarse");
            }
        });
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (registerBool){
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
            Toast.makeText(CustomerLoginActivity.this, "Por favor, ingrese su email.", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(CustomerLoginActivity.this, "Por favor, ingrese su contraseña.", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Iniciando sesión:");
            progressDialog.setMessage("Por favor, espere mientras comprobamos sus datos ...");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(CustomerLoginActivity.this, "Ha ocurrido un error al iniciar sesión. Por favor, intente nuevamente.", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(CustomerLoginActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CustomerLoginActivity.this,CustomerMapActivity.class));
                    }
                }
            });

        }
    }

    public void RegisterUser(){
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(CustomerLoginActivity.this, "Por favor, ingrese su email.", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(CustomerLoginActivity.this, "Por favor, ingrese su contraseña.", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Registrando usuario:");
            progressDialog.setMessage("Por favor espere, estamos registrando sus datos...");
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(CustomerLoginActivity.this, "No se ha podido registrar la cuenta.", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id).child("Email");
                        current_user_db.setValue(email);
                        Toast.makeText(CustomerLoginActivity.this, "Registro de usuario exitoso.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CustomerLoginActivity.this,CustomerProfileActivity.class));
                    }
                }
            });
        }
    }


}