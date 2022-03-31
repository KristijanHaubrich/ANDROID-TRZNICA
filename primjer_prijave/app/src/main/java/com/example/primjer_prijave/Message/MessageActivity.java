package com.example.primjer_prijave.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.primjer_prijave.AddProduct.ImageActivity;
import com.example.primjer_prijave.AddProduct.Product;
import com.example.primjer_prijave.AddProduct.ProductAdapter;
import com.example.primjer_prijave.NotificationMessagingService.APIService;
import com.example.primjer_prijave.NotificationMessagingService.Client;
import com.example.primjer_prijave.NotificationMessagingService.Data;
import com.example.primjer_prijave.NotificationMessagingService.MyResponse;
import com.example.primjer_prijave.NotificationMessagingService.NotificationSender;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.RegistrationLogin.UserProfileActivity;
import com.example.primjer_prijave.SearchUsers.ShowUserActivity;
import com.example.primjer_prijave.SearchUsers.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private int id = 0;
    private String receiver, currentUsername, receiverEmail, receiverProfilePic, receiverToken,type;
    private ImageView iv_send, banner;
    private EditText et_newMessage;
    private TextView tv_receiver_username;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    private List<Message> messages = new ArrayList<>();
    private LinearLayoutManager manager;
    private SearchView searchView;
    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setData();
        InitializeUI();
        setRecycler();
    }

    private void setData() {
        receiver = getIntent().getStringExtra("receiver");
        currentUsername = getIntent().getStringExtra("sender");
        type = getIntent().getStringExtra("type");

    }

    private void InitializeUI() {
        banner = findViewById(R.id.iv_banner);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        tv_receiver_username = findViewById(R.id.holder);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        et_newMessage = findViewById(R.id.et_new_message);
        iv_send = findViewById(R.id.iv_send);
        tv_receiver_username.setText(receiver);


        iv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        tv_receiver_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUserProfile();
            }
        });

        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, UserProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", "message");
                finish();
                startActivity(intent);
            }
        });

        if(type != null){
            tv_receiver_username.setEnabled(false);
            banner.setEnabled(false);
        }
    }

    private void setRecycler() {
        recyclerView.setHasFixedSize(true);

        manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        recyclerView.setLayoutManager(manager);

        fetchData();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    search(newText.toLowerCase());
                    return false;
                }
            });
        }
    }

    private void search(String newText) {
        List<Message> list = new ArrayList<>();
        id = 0;
        for (Message current : messages) {
            if (current.getBody().toLowerCase().contains(newText)) {
                id++;
                current.setId(id);
                list.add(current);
            }
        }

        Comparator<Message> comparator = new Comparator<Message>() {
            @Override
            public int compare(Message left, Message right) {
                return left.getId() - right.getId();
            }
        };

        Collections.sort(list, comparator);


        MessageAdapter adapter = new MessageAdapter(this, list);
        recyclerView.setAdapter(adapter);

    }

    private void sendMessage() {
        if (et_newMessage.getText().toString().isEmpty()) {
            Toast.makeText(MessageActivity.this, "Niste unijeli poruku", Toast.LENGTH_SHORT).show();
        } else {

            String body = et_newMessage.getText().toString();

            Message newMessageReceive = new Message("receive", body, currentUsername, receiver, 0);
            Message newMessageSend = new Message("send", body, currentUsername, receiver, 0);

            if (currentUsername.equals(receiver)) {
                FirebaseDatabase.getInstance().getReference("Messages").child(currentUsername).child(receiver).child(String.valueOf(System.currentTimeMillis())).setValue(newMessageSend);
            } else {
                FirebaseDatabase.getInstance().getReference("Messages").child(currentUsername).child(receiver).child(String.valueOf(System.currentTimeMillis())).setValue(newMessageSend);
                FirebaseDatabase.getInstance().getReference("Messages").child(receiver).child(currentUsername).child(String.valueOf(System.currentTimeMillis())).setValue(newMessageReceive);
            }


            FirebaseDatabase.getInstance().getReference("Tokens").child(receiver).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        receiverToken = snapshot.getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            if (receiverToken != null && !receiver.equals(currentUsername)) sendMessageNotification(newMessageReceive, receiverToken);
            et_newMessage.setText("");

        }

    }

    private void sendMessageNotification(Message message, String mtoken) {

        Data data = new Data(message.getType(), " ", message.getBody(), message.getSender(), message.getReceiver());
        NotificationSender notificationSender = new NotificationSender(data, mtoken);

        apiService.SendNotification(notificationSender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

    private void fetchData() {

        FirebaseDatabase.getInstance().getReference("Messages").child(currentUsername).child(receiver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                if (snapshot.exists()) {
                    id = 0;
                    for (DataSnapshot current : snapshot.getChildren()) {
                        id++;
                        Message message = current.getValue(Message.class);
                        message.setId(id);
                        messages.add(message);
                    }


                    Comparator<Message> comparator = new Comparator<Message>() {
                        @Override
                        public int compare(Message left, Message right) {
                            return right.getId() - left.getId();
                        }
                    };

                    Collections.sort(messages, comparator);

                    adapter = new MessageAdapter(MessageActivity.this, messages);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    private void goToUserProfile(){
        Intent intent = new Intent(this , ShowUserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("username", receiver);
        FirebaseDatabase.getInstance().getReference("Users").child(receiver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    receiverEmail = user.getEmail();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference("profilePics").child(receiver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                   receiverProfilePic = snapshot.getValue().toString();
                   intent.putExtra("email", receiverEmail);
                   startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}