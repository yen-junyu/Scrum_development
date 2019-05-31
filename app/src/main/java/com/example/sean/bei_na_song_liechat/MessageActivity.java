package com.example.sean.bei_na_song_liechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sean.bei_na_song_liechat.Adapter.MessageAdapter;
import com.example.sean.bei_na_song_liechat.Adapter.StickerAdapter;
import com.example.sean.bei_na_song_liechat.Fragments.APIService;
import com.example.sean.bei_na_song_liechat.Model.Chat;
import com.example.sean.bei_na_song_liechat.Model.ChatMessage;
import com.example.sean.bei_na_song_liechat.Model.User;
import com.example.sean.bei_na_song_liechat.Notification.Client;
import com.example.sean.bei_na_song_liechat.Notification.Data;
import com.example.sean.bei_na_song_liechat.Notification.MyResponse;
import com.example.sean.bei_na_song_liechat.Notification.Sender;
import com.example.sean.bei_na_song_liechat.Notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


//***************************** Part.6 Create chat window *****************************
public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser fuser;
    DatabaseReference reference;

    //***************************** Part.8 Display message *****************************
    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;
    //***************************** </Part.8 Display message *****************************

    Intent intent;

    //***************************** Part.7 Get message at firebase *****************************
    ImageButton btn_send;
    ImageButton btn_pic;
    EditText text_send;
    //***************************** </Part.7 Get message at firebase *****************************


    //***************************** Part.14 show message that have been seen *****************************
    ValueEventListener seenListener;
    //***************************** </Part.14 show message that have been seen *****************************
    String userid;
    APIService apiService;
    boolean notify = false;

    //***************************** Part.XXXXXXX*****************************
    private int[] imagesId={R.drawable.weird,R.drawable.a,R.drawable.b,R.drawable.c,R.drawable.d,R.drawable.e,R.drawable.f,R.drawable.g,R.drawable.h};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change here or crash.
                finish();
                //startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        //***************************** Part.18 send notification *****************************
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        //***************************** </Part.18 send notification *****************************
        //***************************** Part.8 Display message *****************************
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //***************************** </Part.8 Display message *****************************

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        btn_pic = findViewById(R.id.btn_pic);
        text_send = findViewById(R.id.text_send);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        //***************************** Part.7 Get message at firebase *****************************
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                ChatMessage msg = new ChatMessage(text_send.getText().toString(),null);
                if (!msg.getTextMessage().equals("")) {
                    sendMessage(fuser.getUid(), userid, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });
        btn_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });
        //***************************** </Part.7 Get message at firebase *****************************


        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
                //***************************** Part.8 Display message *****************************
                readMessages(fuser.getUid(), userid, user.getImageURL());
                //***************************** </Part.8 Display message *****************************
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        seenMessage(userid);
    }

    //***************************** Part.14 show message that have been seen *****************************
    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //***************************** </Part.14 show message that have been seen *****************************


    //***************************** Part.7 Get message at firebase *****************************
    private void sendMessage(String sender, final String receiver, ChatMessage message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);

        final ChatMessage msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
//                .child(fuser.getUid())
//                .child(receiver);

//        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                chatRef.child("id").setValue(receiver);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }
    private void showAlertDialog() {
        // Prepare grid view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog;
        final GridView gridView = new GridView(this);
        ArrayList<Integer> items = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            items.add(imagesId[i]);
        }

       // SimpleAdapter adapter=new SimpleAdapter(this,mList,android.R.layout.simple_list_item_1,new int[]{R.id.image});
        gridView.setAdapter(new StickerAdapter(this, items));
        gridView.setNumColumns(3);
        // Set grid view to alertDialog
        builder.setView(gridView);
        builder.setTitle("STICKER");
        dialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Integer imgID = (Integer) ((StickerAdapter)gridView.getAdapter()).getItem(position);
                dialog.dismiss();
                ChatMessage msg = new ChatMessage(null,imgID);
                sendMessage(fuser.getUid(), userid, msg);
            }
        });
    }
    private void sendNotification(final String receiver, final String username, final ChatMessage message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, username + ": " + message, "New Message", userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    //***************************** </Part.7 Get message at firebase *****************************

    //***************************** Part.8 Display message *****************************
    private void readMessages(final String myid, final String userid, final String imageurl) {
        mchat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        mchat.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //***************************** </Part.8 Display message *****************************

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }


    //***************************** Part.12 Show user status *****************************
    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }
    //***************************** </Part.12 Show user status *****************************


}
//***************************** </Part.6 Create chat window *****************************