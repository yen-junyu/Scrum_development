package com.example.sean.bei_na_song_liechat.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sean.bei_na_song_liechat.Adapter.UsersAdapter;
import com.example.sean.bei_na_song_liechat.Model.User;
import com.example.sean.bei_na_song_liechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter userAdapter;
    //List contains user info which include img and username.
    private List<User> mUsers;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //LayoutInflater can load layout into frame.
        //inflate(layout child layout id, layout parent, boolean the description as belown )
        //False: It will set the default parent layout for child layout.
        //True : Setting the outer layout(parent layout), when the view had been added into the parent view,then the outer layout's setting will become effective.
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        //Set recycleView's layout position.
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    
        mUsers = new ArrayList<>();
        readUsers();
        
        return view;
    }

    private void readUsers() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                //Get the user info according database
                //For loop iterative tracing.
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    assert firebaseUser != null;
                    //If get the user id does not the current user id.
                    //Then push the user info. into ArrayList.
                    if(!user.getId().equals(firebaseUser.getUid())){
                        mUsers.add(user);
                    }
                }
                //Set user int Adapter.
                userAdapter = new UsersAdapter(getContext(), mUsers);
                //Set useradapter into recycleview.
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
