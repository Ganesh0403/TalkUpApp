package com.example.talkup;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private ProgressDialog loadingBar;
    private Toolbar SettingsToolbar;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private StorageReference UserProfileImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });

        RetrieveUserInfo();
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri imageUri = result.getUri();
//                Picasso.with(SettingsActivity.this).load(imageUri).into(userProfileImage);
//                userProfileImage.setImageURI(imageUri);

                final StorageReference filePath = UserProfileImagesRef.child(currentUserId + "."+getFileExtension(imageUri));

                filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SettingsActivity.this, "Profile Image uploaded successfully...", Toast.LENGTH_SHORT).show();
                        if (taskSnapshot.getMetadata() != null) {
                            if (taskSnapshot.getMetadata().getReference() != null) {
                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String imageUrl = uri.toString();
//                                        Log.d("i", "onSuccess: "+imageUrl);
                                        //createNewPost(imageUrl);
                                        RootRef.child("Users").child(currentUserId).child("image")
                                                .setValue(imageUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(SettingsActivity.this, "Image save in Database, Successfully...", Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }else{
                                                            String message =  task.getException().toString();
                                                            Toast.makeText(SettingsActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SettingsActivity.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception e = result.getError();
                Toast.makeText(this, "Possible error is: "+e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void InitializeFields() {
        UpdateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
        SettingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }
    private void UpdateSettings() {
        String setUsername = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUsername) && TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please enter username and status...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(setUsername)){
            Toast.makeText(this, "Please enter username...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please enter status...", Toast.LENGTH_SHORT).show();
        }else{
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUsername);
            profileMap.put("status", setStatus);
            RootRef.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
//                            Log.d("retrieve", "onDataChange: "+retrieveProfileImage);

                            Picasso.with(SettingsActivity.this).load(retrieveImage).into(userProfileImage);


                        }else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                        }else{
                            Toast.makeText(SettingsActivity.this, "Please Set and Update Profile Information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent MainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}