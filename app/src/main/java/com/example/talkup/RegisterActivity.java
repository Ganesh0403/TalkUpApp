package com.example.talkup;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        InitializeFields();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToLoginActivity();
            }
        });
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Email and Password...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Email...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Password...", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we are creating new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                String currentUserId = mAuth.getCurrentUser().getUid();
//                                final String[] deviceToken = new String[1];
//                                FirebaseInstanceId.getInstance().getInstanceId()
//                                        .addOnSuccessListener(RegisterActivity.this, new OnSuccessListener<InstanceIdResult>() {
//                                            @Override
//                                            public void onSuccess(InstanceIdResult instanceIdResult) {
//                                                deviceToken[0] = instanceIdResult.getToken();
//
//                                            }
//                                        });
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                RootRef.child("Users").child(currentUserId).setValue("");

                                RootRef.child("Users").child(currentUserId).child("device_token").setValue(deviceToken);

                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        CreateAccountButton = findViewById(R.id.register_button);
        UserEmail = findViewById(R.id.register_email);
        UserPassword = findViewById(R.id.register_password);
        AlreadyHaveAccountLink = findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this);
    }
    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
    private void sendUserToMainActivity() {
        Intent MainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}