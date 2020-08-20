package com.example.talkup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("TalkUp");

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null){
            sendUserToLoginActivity();
        }else{
            updateUserStatus("online");
            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                    //Toast.makeText(MainActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                }else{
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_option){
            logout();
        }
        if(item.getItemId() == R.id.main_create_group_option){
            RequestNewGroup();
        }
        if(item.getItemId() == R.id.main_settings_option){
            sendUserToSettingsActivity();
        }
        if(item.getItemId() == R.id.main_find_friends_option){
            sendUserToFindFriendsActivity();
        }
        return true;
    }

    private void RequestNewGroup() {
        builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name : ");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Friends");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write Group name...", Toast.LENGTH_SHORT).show();
                }else{
                    CreateNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void CreateNewGroup(final String groupName) {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupName+" group is created successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void logout() {
        builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.confirmation_pop,null);

        Button noButton = view.findViewById(R.id.no_text);
        Button yesButton = view.findViewById(R.id.yes_text);

        builder.setView(view);
        dialog = builder.create();
        dialog.show();

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserStatus("offline");
                mAuth.signOut();
                sendUserToLoginActivity();
                dialog.dismiss();
            }
        });
    }
    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
//        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
//        finish();
    }
    private void sendUserToFindFriendsActivity() {
        Intent FindFriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
//        FindFriendsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(FindFriendsIntent);
//        finish();
    }
    private void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate =new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime =new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", saveCurrentTime);
        onlineState.put("date", saveCurrentDate);
        onlineState.put("state", state);

        currentUserId = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserId).child("userState")
                .updateChildren(onlineState);
    }
}