package com.example.primjer_prijave.SearchUsers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.primjer_prijave.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {


    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> users;
    private androidx.appcompat.widget.SearchView searchView;
    private ValueEventListener userListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);
        InitializeUI(view);
        return view;

    }

    private void InitializeUI(View view){
        searchView = view.findViewById(R.id.searchView);
        setRecycler(view);
    }

    private void setRecycler(View view){
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        users = new ArrayList<>();
        fetchData(view);

        if(searchView != null){
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


    private void fetchData(View view) {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    users.add(user);
                }

                adapter =  new UserAdapter(view.getContext(), users);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.addValueEventListener(userListener);

    }

    private void search(String newText) {
        List<User> list = new ArrayList<>();
        for(User current : users){
            if(current.getUsername().toLowerCase().contains(newText)){
                list.add(current);
            }
        }
        UserAdapter adapter = new UserAdapter(getContext(),list);
        recyclerView.setAdapter(adapter);

    }


}
