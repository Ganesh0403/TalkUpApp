package com.example.talkup;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserId, Current_State, senderUserId;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageRequestButton, DeclineMessageRequestButton;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        Toast.makeText(this, "User id: "+receiverUserId, Toast.LENGTH_SHORT).show();

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton = findViewById(R.id.decline_message_request_button);
        Current_State = "new";

        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && dataSnapshot.hasChild("image")){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.with(ProfileActivity.this).load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    
                    ManageChatRequests();
                }else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequests() {
        ChatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserId)){
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if(request_type.equals("sent")){
                                Current_State = "request_sent";
                                SendMessageRequestButton.setText("Cancel Chat Request");
                            }else if(request_type.equals("received")){
                                Current_State = "request_received";
                                SendMessageRequestButton.setText("Accept Chat Request");

                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }else{
                            ContactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiverUserId)){
                                                Current_State = "friends";
                                                SendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if(!senderUserId.equals(receiverUserId)){
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendMessageRequestButton.setEnabled(false);
                    if(Current_State.equals("new")){
                        SendChatRequest();
                    }
                    if(Current_State.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if(Current_State.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if(Current_State.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });

        }else{
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserId).child(receiverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    ContactsRef.child(receiverUserId).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendMessageRequestButton.setEnabled(true);
                                Current_State = "new";
                                SendMessageRequestButton.setText("Send Message");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ContactsRef.child(receiverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        ChatRequestRef.child(senderUserId).child(receiverUserId)
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    ChatRequestRef.child(receiverUserId).child(senderUserId)
                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            SendMessageRequestButton.setEnabled(true);
                                                            Current_State = "friends";
                                                            SendMessageRequestButton.setText("Remove this Contact");

                                                            DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                            DeclineMessageRequestButton.setEnabled(false);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }

                                }
                            });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    ChatRequestRef.child(receiverUserId).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendMessageRequestButton.setEnabled(true);
                                Current_State = "new";
                                SendMessageRequestButton.setText("Send Message");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ChatRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from",senderUserId);
                                                chatNotificationMap.put("type","request");

                                                NotificationRef.child(receiverUserId).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    SendMessageRequestButton.setEnabled(true);
                                                                    Current_State = "request_sent";
                                                                    SendMessageRequestButton.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}