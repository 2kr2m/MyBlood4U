package com.AndPro.dba.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.lights.LightState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.AndPro.dba.Email.JavaMailApi;
import com.AndPro.dba.Modal.User;
import com.AndPro.dba.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
//in this class an adapter to the user class had been created
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }
// creating the three methods (onCreateViewHolder,onBindViewHolder,getItemCount)

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_displayed_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final User user = userList.get(position);

        holder.type.setText(user.getType());
        if(user.getType().equals("donor")){
            holder.emailNow.setVisibility(View.VISIBLE);
        }
        holder.name.setText(user.getFullName());
        holder.email.setText(user.getEmail());
        holder.phoneNumber.setText(user.getPhoneNumber());
        holder.bloodGroup.setText(user.getBloodGroup());

        Glide.with(context).load(user.getProfilepictureurl()).into(holder.userProfileImage);

        final String nameOfTheReceiver = user.getFullName();
        final String idOfTheReceiver = user.getId();

        //sending email
        holder.emailNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("SEND EMAIL")
                        .setMessage("send mail to"+user.getFullName()+"?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String nameOfSender=snapshot.child("fullName").getValue().toString();
                                        String emailOfSender=snapshot.child("email").getValue().toString();
                                        String phone=snapshot.child("phoneNumber").getValue().toString();
                                        String blood=snapshot.child("bloodGroup").getValue().toString();

                                        String mEmail = user.getEmail();
                                        String mSubject = "Blood Donation";
                                        String mMessage = "Hello "+nameOfTheReceiver+" , "+nameOfSender+
                                                "would like blood donation from you .Here's his/her details :\n"+
                                                "Name :"+nameOfSender+"\n"+
                                                "Phone number :"+phone+"\n"+
                                                "Email :"+emailOfSender+"\n"+
                                                "Blood group :"+blood+"\n"+
                                                "Kindly rich out to him/her .Thank you !\n"+
                                                "Blood Donation App - Donate BLOOD , SAVE LIVES!!";
                                        JavaMailApi javaMailApi = new JavaMailApi(context,mEmail,mSubject,mMessage);
                                        javaMailApi.execute();

                                        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("emails")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        senderRef.child(idOfTheReceiver).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    DatabaseReference receiverRef=FirebaseDatabase.getInstance().getReference("emails")
                                                            .child(idOfTheReceiver);
                                                    receiverRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);

                                                    addNotification(idOfTheReceiver,FirebaseAuth.getInstance().getCurrentUser().getUid());

                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("No",null)
                        .show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
//fixing the elements giving the shape of the displayed users
    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView userProfileImage;
        public TextView type,name,email,phoneNumber,bloodGroup;
        public Button emailNow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileImage=itemView.findViewById(R.id.userProfileImage);
            type=itemView.findViewById(R.id.type);
            name=itemView.findViewById(R.id.name);
            email=itemView.findViewById(R.id.email);
            phoneNumber=itemView.findViewById(R.id.phoneNumber);
            bloodGroup=itemView.findViewById(R.id.bloodGroup);


        }
    }

    //method to add notifications to firebase
    private void addNotification(String receiverId,String senderId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("notifications").child(receiverId);
        String date = DateFormat.getDateInstance().format(new Date());
        HashMap<String,Object> hashMap = new HashMap<String,Object>();
        hashMap.put("receiverId",receiverId);
        hashMap.put("senderId",senderId);
        hashMap.put("text","You have an email,Please check it out!");
        hashMap.put("date",date);

        reference.push().setValue(hashMap);
    }

}
