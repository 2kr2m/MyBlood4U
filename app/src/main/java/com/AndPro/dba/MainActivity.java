package com.AndPro.dba;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.AndPro.dba.Adapter.UserAdapter;
import com.AndPro.dba.Modal.User;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //initializing variables linked to drawerLayout,toolbar and Navigationview
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView nav_view;

    //initializing variables
    private CircleImageView nav_user_image;
    private TextView nav_user_fullname,nav_user_email,nav_user_bloodgroup,nav_user_type;

    //initializing variables linked to RecyclerView and ProgressBar
    private RecyclerView recyclerview;
    private ProgressBar progressbar;

    //initializing variables linked to class user in models and class userAdapter in Adapter
    private List<User> userList;
    private UserAdapter userAdapter;

    //initializing variable to deal with DatabaseReference
    private DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Blood Donation app");

        drawerLayout=findViewById(R.id.drawerLayout);
        nav_view=findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle =new ActionBarDrawerToggle(MainActivity.this,drawerLayout,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);

        //adding drawer listener
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        nav_view.setNavigationItemSelectedListener(this);

        progressbar=findViewById(R.id.progressbar);
        recyclerview=findViewById(R.id.recyclerview);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerview.setLayoutManager(layoutManager);

        //creating userlist and adapter
        userList=new ArrayList<>();
        userAdapter=new UserAdapter(MainActivity.this,userList);
        recyclerview.setAdapter(userAdapter);

        //targetting the logged user
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference().child("users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type=snapshot.child("type").getValue().toString();
                //by type,the program decide with users will displayed
                if (type.equals("donor")){
                    //displaying recipients
                    readRecipients();
                }else{
                    //displaying donors
                    readDonors();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //linking the views
        nav_user_image = nav_view.getHeaderView(0).findViewById(R.id.nav_user_image);
        nav_user_fullname = nav_view.getHeaderView(0).findViewById(R.id.nav_user_fullname);
        nav_user_email = nav_view.getHeaderView(0).findViewById(R.id.nav_user_email);
        nav_user_bloodgroup = nav_view.getHeaderView(0).findViewById(R.id.nav_user_bloodgroup);
        nav_user_type = nav_view.getHeaderView(0).findViewById(R.id.nav_user_type);

        userRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //filling the fields with the user's data
                if (snapshot.exists()){
                    String name = snapshot.child("fullName").getValue().toString();
                    nav_user_fullname.setText(name);
                    String email = snapshot.child("email").getValue().toString();
                    nav_user_email.setText(email);
                    String bloodgroup = snapshot.child("bloodGroup").getValue().toString();
                    nav_user_bloodgroup.setText(bloodgroup);
                    String type = snapshot.child("type").getValue().toString();
                    nav_user_type.setText(type);
                    if(snapshot.hasChild("profilepictureurl")){
                        String imageUrl = snapshot.child("profilepictureurl").getValue().toString();
                        //glide have been imlemented in the gradle
                        Glide.with(getApplicationContext()).load(imageUrl).into(nav_user_image);
                    }else{
                        nav_user_image.setImageResource(R.drawable.profile1);
                    }

                    Menu nav_menu= nav_view.getMenu();
                    if (type.equals("donor")){
                        //only if the type of user is a doner ,the app shows the received emails
                        nav_menu.findItem(R.id.sentemail).setTitle("Received Emails");
                        //only if the type of user is a doner ,the app shows the notifications
                        nav_menu.findItem(R.id.notifications).setVisible(true);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //this method displaying donors
    private void readDonors() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("users");
        Query query=reference.orderByChild("type").equalTo("donor");
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()) {

                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);
                }

                    userAdapter.notifyDataSetChanged();
                    progressbar.setVisibility(View.GONE);

                    if (userList.isEmpty()){
                        Toast.makeText(MainActivity.this,"No Donors", Toast.LENGTH_SHORT).show();
                        progressbar.setVisibility(View.GONE);
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //this method displaying recipients
    private void readRecipients() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("users");
        Query query=reference.orderByChild("type").equalTo("recipient");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()) {

                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);
                    userAdapter.notifyDataSetChanged();
                    progressbar.setVisibility(View.GONE);
                }

                    if (userList.isEmpty()){
                        Toast.makeText(MainActivity.this, "No Recipients", Toast.LENGTH_SHORT).show();
                        progressbar.setVisibility(View.GONE);
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()){
        //get donors/recipients with blood group a+
        case R.id.aplus:
            Intent intent1= new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent1.putExtra("group","A+");
            startActivity(intent1);
            break;
        //get donors/recipients with blood group a-
        case R.id.aminus:
            Intent intent2= new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent2.putExtra("group","A-");
            startActivity(intent2);
            break;
        //get donors/recipients with blood group b+
        case R.id.bplus:
            Intent intent3= new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent3.putExtra("group","B+");
            startActivity(intent3);
            break;
        //get donors/recipients with blood group b-
        case R.id.bminus:
            Intent intent4= new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent4.putExtra("group","B-");
            startActivity(intent4);
            break;
        //get donors/recipients with blood group ab+
        case R.id.abplus:
            Intent intent5= new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent5.putExtra("group","AB+");
            startActivity(intent5);
            break;
        //get donors/recipients with blood group ab-
        case R.id.abminus:
            Intent intent6= new Intent( MainActivity.this,CategorySelectedActivity.class);
            intent6.putExtra("group","AB-");
            startActivity(intent6);
            break;
        //get donors/recipients with blood group o+
        case R.id.oplus:
            Intent intent7= new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent7.putExtra("group","O+");
            startActivity(intent7);
            break;
        //get donors/recipients with blood group o-
        case R.id.ominus:
            Intent intent8= new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent8.putExtra("group","O-");
            startActivity(intent8);
            break;
        //get donors/recipients with compatible blood group with the user
        case R.id.compatible:
            Intent intent11= new Intent(MainActivity.this,CategorySelectedActivity.class);
            intent11.putExtra("group","Compatible with me");
            startActivity(intent11);
            break;
        //get mails
        case R.id.sentemail:
            Intent intent12= new Intent(MainActivity.this,SentEmailActivity.class);
            startActivity(intent12);
            break;
        //get notifications
        case R.id.notifications:
            Intent intent13= new Intent(MainActivity.this,NotificationActivity.class);
            startActivity(intent13);
            break;
        //display user profile
        case R.id.profile:
            Intent intent9= new Intent(MainActivity.this,ProfileActivity.class);
            startActivity(intent9);
            break;

        //logout
        case R.id.logout:
            FirebaseAuth.getInstance().signOut();
            Intent intent10=new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent10);
            break;

    }
    drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}