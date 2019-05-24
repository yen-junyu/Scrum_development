package com.example.sean.bei_na_song_liechat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sean.bei_na_song_liechat.Fragments.ChatsFragment;
import com.example.sean.bei_na_song_liechat.Fragments.ProfileFragment;
import com.example.sean.bei_na_song_liechat.Fragments.UsersFragment;
import com.example.sean.bei_na_song_liechat.Model.Chat;
import com.example.sean.bei_na_song_liechat.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    //User identify.
    FirebaseUser firebaseUser;
    //public class DatabaseReference extends Query.
    //Firebase reference represents a particular location in your Database
    //and can be used for reading or writing data to that Database location/
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the activity_main layout into the FrameLayout.
        //if the main layout's layout_weight & layout_height not match parent.
        //Then its layout's size which fit the widget in the layout.
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        //Catch the current user who is using.
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());


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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //***************************** Part.3 Tablayout start *****************************
        //Layout which manage the page view.
        //that allows the user to flip left and right through pages of data.
        //Suppling an implementation of a PagerAdapter to generate the pages that the view shows.
        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                        unread++;
                    }
                }
                if (unread == 0) {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                } else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "(" + unread + ") Chats");
                }

                //Add Chat and Users Fragments.
                viewPagerAdapter.addFragment(new UsersFragment(), "Users");
                viewPagerAdapter.addFragment(new ProfileFragment(), "Profile"); //Part.10 profile.


                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //***************************** Part.3 Tablayout end *****************************
    }

    //***************************** Part.2 login & logout bar start *****************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                // change here or crash.
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
        }
        return false;
    }
    //***************************** Part.2 login & logout bar end *****************************

    //***************************** Part.3 Tablayout start *****************************
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
    //***************************** </Part.3 Tablayout end *****************************

    //***************************** Part.12 Show user status *****************************
    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
    //***************************** </Part.12 Show user status *****************************
}
