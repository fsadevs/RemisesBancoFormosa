package com.fsadevs.remisesbancoformosa.Customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fsadevs.remisesbancoformosa.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerProfileActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField, mDocketField, mCecoField;
    private Button mBack, mConfirm;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userID, mName, mDocket, mCeco, mPhone;
    private String mProfileImageUrl;
    private Uri resultUri;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        mNameField =  findViewById(R.id.tbx_customer_nombre);
        mPhoneField =  findViewById(R.id.tbx_customer_celular);
        mDocketField = findViewById(R.id.tbx_customer_legajo);
        mCecoField = findViewById(R.id.tbx_customer_ceco);
        mProfileImage = findViewById(R.id.profile_image);
        mBack =  findViewById(R.id.btn_customer_settings_cancel);
        mConfirm =  findViewById(R.id.btn_customer_settings_ok);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        progressDialog = new ProgressDialog(this);

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(CustomerProfileActivity.this);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerProfileActivity.this,CustomerMapActivity.class));
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.left_out);

            }
        });
    }
    private void getUserInfo(){
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("phone")!=null){
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if(map.get("ceco")!=null){
                        mCeco = map.get("ceco").toString();
                        mCecoField.setText(mCeco);
                    }
                    if(map.get("docket")!=null){
                        mDocket = map.get("docket").toString();
                        mDocketField.setText(mDocket);
                    }
                    if(map.get("profileImageUrl")!=null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Picasso.get().load(mProfileImageUrl).into(mProfileImage);                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            { }
        });
    }



    private void saveUserInformation() {
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mDocket = mDocketField.getText().toString();
        mCeco = mCecoField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("docket",mDocket);
        userInfo.put("ceco",mCeco);
        mCustomerDatabase.updateChildren(userInfo);

        if(resultUri != null) {
            progressDialog.setTitle("Actualizando foto de perfil:");
            progressDialog.setMessage("Por favor, espere mientras se guarda su imágen en el servidor...");
            progressDialog.show();
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CustomerProfileActivity.this, "Ha ocurrido un error actualizando su información.", Toast.LENGTH_SHORT).show();
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("profileImageUrl", uri.toString());
                            mCustomerDatabase.updateChildren(newImage);
                            progressDialog.dismiss();
                            Toast.makeText(CustomerProfileActivity.this,"Se han guardado los cambios.",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CustomerProfileActivity.this,CustomerMapActivity.class));
                            finish();
                            overridePendingTransition(R.anim.left_in, R.anim.left_out);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(CustomerProfileActivity.this, "Ha ocurrido un error al guardar su imágen.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        else{
            Toast.makeText(CustomerProfileActivity.this,"Se han guardado los cambios.",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CustomerProfileActivity.this,CustomerMapActivity.class));
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.left_out);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE  &&  resultCode==RESULT_OK  &&  data!=null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            resultUri = result.getUri();
            mProfileImage.setImageURI(resultUri);
        }
        else {
            Toast.makeText(this, "Ha ocurrido un error al seleccionar la imágen.", Toast.LENGTH_SHORT).show();
        }
    }
}
