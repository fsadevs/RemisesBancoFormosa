package com.fsadevs.remisesbancoformosa.Driver;

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

public class DriverProfileActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField, mDominioField, mProveedorField;
    private Button mBack, mConfirm;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;
    private String userID, mName, mDominio, mProveedor, mPhone;
    private String mProfileImageUrl;
    private Uri resultUri;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        mNameField =  findViewById(R.id.tbx_driver_nombre);
        mPhoneField =  findViewById(R.id.tbx_driver_celular);
        mDominioField = findViewById(R.id.tbx_driver_dominio);
        mProveedorField = findViewById(R.id.tbx_driver_proveedor);
        mProfileImage = findViewById(R.id.profile_image);
        mBack =  findViewById(R.id.btn_driver_settings_cancel);
        mConfirm =  findViewById(R.id.btn_driver_settings_ok);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID);
        progressDialog = new ProgressDialog(this);

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(DriverProfileActivity.this);
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
                startActivity(new Intent(DriverProfileActivity.this,DriverMapActivity.class));
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
            }
        });
    }
    //----------------------------------------------------------------------------------------------
    private void getUserInfo(){
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
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
                    if(map.get("proveedor")!=null){
                        mProveedor = map.get("proveedor").toString();
                        mProveedorField.setText(mProveedor);
                    }
                    if(map.get("dominio")!=null){
                        mDominio = map.get("dominio").toString();
                        mDominioField.setText(mDominio);
                    }
                    if(map.get("profileImageUrl")!=null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Picasso.get().load(mProfileImageUrl).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            { }
        });
    }

    //----------------------------------------------------------------------------------------------
    private void saveUserInformation() {
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mDominio = mDominioField.getText().toString();
        mProveedor = mProveedorField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("dominio",mDominio);
        userInfo.put("proveedor",mProveedor);
        mDriverDatabase.updateChildren(userInfo);

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
                    Toast.makeText(DriverProfileActivity.this, "Ha ocurrido un error al actualizar su información.", Toast.LENGTH_SHORT).show();
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
                            mDriverDatabase.updateChildren(newImage);
                            progressDialog.dismiss();
                            Toast.makeText(DriverProfileActivity.this,"Se han guardado los cambios.",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(DriverProfileActivity.this,DriverMapActivity.class));
                            finish();
                            overridePendingTransition(R.anim.left_in, R.anim.left_out);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(DriverProfileActivity.this, "Ha ocurrido un error al guardar su imágen.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }else {
            Toast.makeText(DriverProfileActivity.this, "Se han guardado los cambios.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DriverProfileActivity.this, DriverMapActivity.class));
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.left_out);
        }


    }

    //----------------------------------------------------------------------------------------------
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
//--------------------------------------------------------------------------------------------------
}
