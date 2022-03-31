package com.example.primjer_prijave.Message;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.primjer_prijave.AddProduct.ImageActivity;
import com.example.primjer_prijave.AddProduct.ProductShowUserAdapter;
import com.example.primjer_prijave.R;
import com.example.primjer_prijave.RegistrationLogin.UserProfileActivity;
import com.example.primjer_prijave.SearchUsers.User;
import com.example.primjer_prijave.SearchUsers.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

import static android.content.ContentValues.TAG;

public class MessageFragment extends Fragment {


    private RecyclerView recyclerView;
    private InboxAdapter adapter;
    private boolean check = false;
    private List<String> users = new ArrayList<>();

    private androidx.appcompat.widget.SearchView searchView;
    private ValueEventListener userListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        InitializeUI(view);
        return view;

    }

    private void InitializeUI(View view) {

        searchView = view.findViewById(R.id.searchView);
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    setRecycler(view, snapshot.getValue(User.class).getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void setRecycler(View view, String username) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        users = new ArrayList<>();
        fetchData(view,username);

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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


    private void fetchData(View view, String username) {
        FirebaseDatabase.getInstance().getReference("Messages").child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    users.clear();
                    for(DataSnapshot current:snapshot.getChildren()){
                        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    for(DataSnapshot snap:snapshot.getChildren()){
                                        if(snap.exists() && snap.getValue(User.class).getUsername().equals(current.getKey())) check = true;
                                    }
                                    if(check == false) FirebaseDatabase.getInstance().getReference("Messages").child(username).child(current.getKey()).removeValue();
                                    check = false;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        if(current != null){
                            String inboxUser = current.getKey();
                            users.add(inboxUser);
                        }

                    }

                    adapter = new InboxAdapter(view.getContext(),users);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void search(String newText) {
        List<String> list = new ArrayList<>();
        for(String current : users){
            if(current.toLowerCase().contains(newText)){
                list.add(current);
            }
        }
        InboxAdapter adapter = new InboxAdapter(getContext(),list);
        recyclerView.setAdapter(adapter);

    }
}
