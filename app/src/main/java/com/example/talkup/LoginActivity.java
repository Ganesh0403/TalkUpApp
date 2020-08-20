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

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private Button LoginButton,PhoneLoginButton;
    private EditText UserEmail,UserPassword;
    private TextView NeedNewAccountLink,ForgetPasswordLink;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });

        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneLoginIntent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
//                finish();
            }
        });

    }

    private void AllowUserToLogin() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Email and Password...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Email...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Password...", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                String currentUserId = mAuth.getCurrentUser().getUid();
//                                final String[] deviceToken = new String[1];
//                                FirebaseInstanceId.getInstance().getInstanceId()
//                                        .addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
//                                            @Override
//                                            public void onSuccess(InstanceIdResult instanceIdResult) {
//                                                deviceToken[0] = instanceIdResult.getToken();
//                                            }
//                                        });
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                UsersRef.child(currentUserId).child("device_token")
                                        .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            sendUserToMainActivity();
                                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
        finish();
    }

    private void InitializeFields() {
        LoginButton = findViewById(R.id.login_button);
        PhoneLoginButton = findViewById(R.id.phone_login_button);
        UserEmail = findViewById(R.id.login_email);
        UserPassword = findViewById(R.id.login_password);
        NeedNewAccountLink = findViewById(R.id.need_new_account_link);
        ForgetPasswordLink = findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);
    }


    private void sendUserToMainActivity() {
        Intent MainIntent = new Intent(LoginActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}