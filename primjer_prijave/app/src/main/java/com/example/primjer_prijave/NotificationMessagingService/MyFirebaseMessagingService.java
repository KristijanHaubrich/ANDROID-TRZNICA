package com.example.primjer_prijave.NotificationMessagingService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.primjer_prijave.Message.Message;
import com.example.primjer_prijave.Message.MessageActivity;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.RegistrationLogin.MainActivity;
import com.example.primjer_prijave.SearchUsers.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private boolean notify = true;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        String type = remoteMessage.getData().get("type");
        String sender = remoteMessage.getData().get("sender");
        String receiver = remoteMessage.getData().get("receiver");

        if(type.equals("notification")){
            pushNotification(title,message);
        }else{
            pushMessageNotification(sender,message,receiver);
        }


    }


    private void pushMessageNotification(String sender, String message, String receiver) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }

        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot current:snapshot.getChildren()){
                        User user = current.getValue(User.class);
                        if(user.getUsername().equals(receiver)){
                            FirebaseDatabase.getInstance().getReference("Passwords").child(receiver).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists() && notify){
                                        String password = snapshot.getValue().toString();
                                        FirebaseAuth.getInstance().signInWithEmailAndPassword(receiver,password);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Intent notificationIntent = new Intent(this, MessageActivity.class);
        notificationIntent.putExtra("receiver", sender);
        notificationIntent.putExtra("sender",receiver);
        notificationIntent.putExtra("type","notification");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"n")
                .setContentTitle("Nova poruka od " + sender)
                .setSmallIcon(R.drawable.ic_notify)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentText(message);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        managerCompat.notify(999,builder.build());

    }

    private void pushNotification(String title, String message){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"n")
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notify)
                .setAutoCancel(true)
                .setContentText(message);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        managerCompat.notify(999,builder.build());
    }


}
